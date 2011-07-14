package edu.gemini.aspen.gds.web.ui.keywords.model

import com.vaadin.data.Item
import com.vaadin.ui.TextField
import com.vaadin.data.validator.AbstractStringValidator
import edu.gemini.aspen.gds.api.{FitsComment, GDSConfiguration}

/**
 * PropertyItemWrapperFactory for FitsKeyword that uses a TextField to make possible to edit
 * the name of a FITS Keyword
 */
class FitsCommentPropertyFactory extends PropertyItemWrapperFactory(classOf[FitsComment], classOf[TextField]) {
  val validator = new AbstractStringValidator("Value {0} must be less than 8 characters") {
    // todo check the lenght
    def isValidString(value: String) = value.length <= 80
  }

  override def createItemAndWrapper(config: GDSConfiguration, item: Item) = {
    val textField = new TextField("", config.fitsComment.value.toString)
    textField.addValidator(validator)
    textField.setImmediate(true)
    textField.setRequired(true)
    textField.setInvalidAllowed(false)

    def wrapper(config: GDSConfiguration) = {
      config.copy(fitsComment = FitsComment(textField.getValue.toString))
    }

    (textField, wrapper)
  }
}