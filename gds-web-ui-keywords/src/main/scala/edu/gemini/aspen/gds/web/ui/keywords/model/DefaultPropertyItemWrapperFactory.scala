package edu.gemini.aspen.gds.web.ui.keywords.model

import com.vaadin.data.Item
import edu.gemini.aspen.gds.api.GDSConfiguration

/**
 * Default implementation of PropertyItemWrapperFactory
 *
 * Should disappear
 */
class DefaultPropertyItemWrapperFactory(clazz: Class[_]) extends PropertyItemWrapperFactory(clazz, classOf[String]) {
  override def createItemAndWrapper(config: GDSConfiguration, item: Item) = {
    null
  }
}












