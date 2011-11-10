package edu.gemini.aspen.gds.web.ui.modules

import edu.gemini.aspen.gds.web.ui.api.GDSWebModule
import com.vaadin.Application
import com.vaadin.ui.{Alignment, VerticalLayout, Label}

/**
 * Tab containing an About message
 */
class AboutModule extends GDSWebModule {
  val title = "About"
  val order = 5

  override def buildTabContent(app: Application): com.vaadin.ui.Component = {
    val aboutLabel = new Label("About GDS")
    aboutLabel.setStyleName("about")
    val layout = new VerticalLayout
    layout.setSizeFull
    layout.addComponent(aboutLabel)
    layout.setComponentAlignment(aboutLabel, Alignment.TOP_CENTER)
    layout
  }
}