package edu.gemini.aspen.gds.web.ui.keywords

import edu.gemini.aspen.gds.web.ui.api.GDSWebModule
import edu.gemini.aspen.gds.api.configuration.GDSConfigurationService
import model.GDSKeywordsDataSource
import edu.gemini.aspen.gds.web.ui.api.VaadinUtilities._
import com.vaadin.ui.Window.Notification
import com.vaadin.ui._
import com.jensjansson.pagedtable.PagedTable
import scala.collection.JavaConversions._
import com.vaadin.data.util.IndexedContainer._
import com.vaadin.ui.Table.ColumnGenerator
import com.vaadin.terminal.ThemeResource
import themes.BaseTheme

/**
 * Module for the table to edit the keywords
 */
class KeywordsTableModule(configService: GDSConfigurationService) extends GDSWebModule {
  val title = "Keyword Configuration"
  val order = 0
  val dataSource = new GDSKeywordsDataSource(configService.getConfiguration)
  var deleteIcon = new ThemeResource("../runo/icons/16/document-delete.png")
  var deleteTooltip = "Delete row"
  val deleteProperty = "DEL"

  def buildValidateButton(mainWindow: Window): Button = {
    val button: Button = new Button("Validate")

    button.addListener((e: Button#ClickEvent) => {
      mainWindow.showNotification("Validating...", Notification.TYPE_HUMANIZED_MESSAGE)
      println(dataSource.toGDSConfiguration.head)
    })
    button
  }

  def setupDeleteColumn(table: PagedTable) {
    table.addGeneratedColumn(deleteProperty, new ColumnGenerator {
      def generateCell(source: Table, itemId: AnyRef, columnId: AnyRef) = {
        val deleteButton = new Button("")
        deleteButton.setStyleName(BaseTheme.BUTTON_LINK)
        deleteButton.setIcon(deleteIcon)
        deleteButton.setDescription(deleteTooltip)
        deleteButton.addListener((e: Button#ClickEvent) => {
          dataSource.removeItem(itemId)
          println("htoeo")
        })
        deleteButton
      }
    })
    table.setColumnIcon(deleteProperty, deleteIcon)
    table.setColumnHeader(deleteProperty, "")
    table.setColumnAlignment(deleteProperty, Table.ALIGN_CENTER)
  }

  override def buildTabContent(mainWindow: Window) = {
    val layout = new VerticalLayout

    val table = new PagedTable("Keywords")
    table.setContainerDataSource(dataSource)
    //table.setSelectable(true)
    table.setNullSelectionAllowed(false)
    table.setImmediate(true)
    table.addStyleName("keywords-table")
    table.setSizeFull
    table.setPageLength(25)
    table.setCacheRate(0.2)
    table.setEditable(true)
    table.setColumnCollapsingAllowed(true)

    setupDeleteColumn(table)

    //layout.

    // Center each column
    dataSource.getContainerPropertyIds foreach {
      table.setColumnAlignment(_,
        Table.ALIGN_LEFT);
    }

    layout.addComponent(table)
    layout.setExpandRatio(table, 1.0f)

    val button = buildValidateButton(mainWindow)

    layout.addComponent(table.createControls())
    layout.addComponent(button)
    layout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT)
    layout.setSizeFull
    layout
  }
}