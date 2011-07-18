package edu.gemini.aspen.gds.web.ui.vaadin

import com.vaadin.Application;
import java.util.logging.Logger
import com.vaadin.terminal.ClassResource
import edu.gemini.aspen.gds.web.ui.api.VaadinUtilities._
import org.apache.felix.ipojo.annotations.{Requires, Bind, Unbind}
import com.vaadin.ui.themes.BaseTheme
import com.vaadin.data.util.ObjectProperty
import edu.gemini.aspen.gds.web.ui.api.{GDSWebModuleFactory, StatusPanel, GDSWebModule}
import javax.swing.text.TabExpander
import com.vaadin.ui._

/**
 * Main page of the GDS web UI
 */
@org.apache.felix.ipojo.annotations.Component(name = "VaadinAppFactory")
class GDSCoreVaadinApp(@Requires statusPanel: StatusPanel) extends Application {
  private val LOG = Logger.getLogger(this.getClass.getName)
  val tabsSheet = new TabSheet()
  val mainWindow = new Window("GDS Management Console")
  val userPanel = buildUserPanel
  var loginPanel = buildLoginPanel
  val userProperty = new ObjectProperty[String]("")

  val gdsWebModules = scala.collection.mutable.Map[GDSWebModuleFactory, (GDSWebModule, TabSheet.Tab)]()

  /**
   * Called by Vaadin when the application needs to start
   *
   * In this case we layout the main components
   */
  override def init() {
    LOG.info("GDSCoreVaadinApp init> ")
    setTheme("gds")
    tabsSheet.setHeight("100%")

    val mainLayout = new VerticalLayout
    mainLayout.setMargin(true)
    mainLayout.setSizeFull

    mainLayout.addComponent(buildTopPanel)

    mainLayout.addComponent(tabsSheet)
    mainLayout.setExpandRatio(tabsSheet, 1.0f)

    mainLayout.addComponent(statusPanel.buildStatusPanel)

    mainWindow.setContent(mainLayout)
    setMainWindow(mainWindow)
  }

  /**
   * Listens for modules making up the tabs
   */
  @Bind(id = "gds-modules", optional = true, aggregate = true, specification = "edu.gemini.aspen.gds.web.ui.api.GDSWebModuleFactory")
  def bindGDSWebModule(moduleFactory: GDSWebModuleFactory) {
    LOG.info("GDSCoreVaadinApp> tab module factory detected " + moduleFactory)

    // Adds the tab built by the moduleFactory
    val gdsModule = moduleFactory.buildWebModule
    val tabContent = gdsModule.buildTabContent(this)
    tabContent.setDebugId(gdsModule.title)
    val tab = tabsSheet.addTab(tabContent, gdsModule.title, null)
    gdsWebModules += moduleFactory -> (gdsModule, tab)
    putTabsInOrder
  }

  def sortModules = {
    gdsWebModules.values.toList sortBy {
      case (m, t) => m.order
    }
  }

  def findTabsPositions(sortedModules: List[(GDSWebModule, TabSheet.Tab)]) = {
    for {i <- 0 until sortedModules.size
         val (_, t) = sortedModules(i)}
    yield (t, i)
  }

  def putTabsInOrder {
    val sortedModules = sortModules
    val tabs = findTabsPositions(sortedModules)
    tabs foreach {
      case (t, i) => tabsSheet.setTabPosition(t, i)
    }
    // Set first as selected
    tabs.headOption map {
      case (t, _) => tabsSheet.setSelectedTab(t.getComponent)
    }
  }

  /**
   * Listens for services gone
   */
  @Unbind(id = "gds-modules", specification = "edu.gemini.aspen.gds.web.ui.api.GDSWebModuleFactory")
  def unbindModule(moduleFactory: GDSWebModuleFactory) {
    LOG.info("GDSCoreVaadinApp> tab module factory gone " + moduleFactory)

    gdsWebModules remove (moduleFactory) foreach {
      case (module, tab) => tabsSheet.removeTab(tab)
    }
    putTabsInOrder
  }

  /**
   * Builds the panel at the top of the application
   */
  private def buildTopPanel = {
    val layout = new VerticalLayout
    layout.setDebugId("Top-Panel")
    layout.setMargin(false)
    layout.addComponent(loginPanel)
    layout.addComponent(userPanel)
    layout.addComponent(buildBannerPanel)

    toggleUserBasedVisibility

    new Panel(layout)
  }

  /**
   * Decides what panel to show depending on whether there is a user logged or not
   */
  private def toggleUserBasedVisibility {
    val user = Option(getUser)

    user map {
      _ =>
        userPanel.setVisible(true)
        loginPanel.setVisible(false)
    } getOrElse {
      userPanel.setVisible(false)
      loginPanel.setVisible(true)
    }
  }

  def buildLoginPanel = {
    val layout = new HorizontalLayout
    layout.setDebugId("Login-Panel")
    val linkButton = new Button("Login")
    linkButton.setStyleName(BaseTheme.BUTTON_LINK)
    linkButton.addStyleName("gds-login-label")

    linkButton.addListener((e: Button#ClickEvent) => mainWindow.addWindow(new LoginWindow(this)))

    layout.addComponent(linkButton)
    layout.setComponentAlignment(linkButton, Alignment.MIDDLE_RIGHT)
    layout.setWidth("100%")
    layout.setHeight("20px")
    layout
  }

  def buildUserPanel = {
    val layout = new HorizontalLayout
    layout.setDebugId("User-Panel")
    val subLayout = new HorizontalLayout
    subLayout.setDebugId("User-SubPanel")

    val userLabel = new Label("User: ")
    //userLabel.setPropertyDataSource(userProperty)
    val logoutButton = new Button("Logout")
    logoutButton.setStyleName(BaseTheme.BUTTON_LINK)

    subLayout.addComponent(userLabel)
    subLayout.addComponent(logoutButton)
    subLayout.setWidth("100%")

    layout.addComponent(subLayout)
    layout.setComponentAlignment(subLayout, Alignment.MIDDLE_RIGHT)
    layout.setWidth("100%")
    layout.setHeight("20px")

    layout
  }

  def buildBannerPanel = {
    val layout = new HorizontalLayout
    layout.setDebugId("Banner-Layout")

    // Add the GDS Label
    val gdsLabel = new Label("GIAPI Data Service")
    layout.setHeight("95px")
    gdsLabel.setStyleName("gds-title")

    layout.addComponent(gdsLabel)
    layout.setComponentAlignment(gdsLabel, Alignment.MIDDLE_LEFT)
    layout.setExpandRatio(gdsLabel, 1.0f)

    layout.setWidth("100%")

    // Add the logo
    val image = new Embedded(null, new ClassResource("gemini-logo.jpg", this))
    image.setHeight("95px")
    image.setWidth("282px")
    image.setStyleName("gds-title")
    layout.addComponent(image)
    layout.setComponentAlignment(gdsLabel, Alignment.MIDDLE_RIGHT)

    layout
  }

  /**
   * Called whet the user completes authentication
   */
  def authenticated(user: String) {
    this.setUser(user)
    userProperty.setValue(user)
    toggleUserBasedVisibility
    // Inform app changes
    gdsWebModules.values.toList map {
      case (m, _) => m.userChanged(user)
    }
  }

}
