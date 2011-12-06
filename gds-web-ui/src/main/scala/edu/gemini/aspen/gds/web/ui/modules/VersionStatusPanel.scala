package edu.gemini.aspen.gds.web.ui.modules

import org.apache.felix.ipojo.annotations.{Provides, Instantiate}
import edu.gemini.aspen.gds.web.ui.api.StatusPanelModule
import edu.gemini.aspen.giapi.web.ui.vaadin.data._

@org.apache.felix.ipojo.annotations.Component
@Instantiate
@Provides(specifications = Array(classOf[StatusPanelModule]))
class VersionStatusPanel extends AbstractStatusPanelModule {
  override val order = 1
  val label = "Version:"
  val item = System.getProperty("gmp.version")
  val property = Property[String](item)
}