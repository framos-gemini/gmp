package edu.gemini.aspen.gds.web.ui.modules

import org.apache.felix.ipojo.annotations.{Provides, Instantiate, Component}
import edu.gemini.aspen.gds.web.ui.api.GDSWebModule
import com.vaadin.ui.{Panel, Window}

@Component
@Instantiate
@Provides(specifications = Array[Class[_]](classOf[GDSWebModule]))
class HelpModule extends GDSWebModule {
    val title = "GDS Help"
    val order = 1

    override def buildTabContent(mainWindow:Window) = new Panel()
}