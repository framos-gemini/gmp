package edu.gemini.aspen.giapi.web.ui.vaadin

import com.vaadin.data.Property
import com.vaadin.terminal.Resource

package components {

import com.vaadin.ui.themes.BaseTheme

/**
 * Scala wrapper for com.vaadin.ui.Label */
class Label(content: String = null, caption: String = null, contentMode: Int = com.vaadin.ui.Label.CONTENT_DEFAULT, style: String = null, property: Property = null) extends com.vaadin.ui.Label(content, contentMode) {
  setStyleName(style)
  setCaption(caption)
  Option(property) map setPropertyDataSource
}

/**
 * Scala wrapper for com.vaadin.ui.Button */
class Button(caption: String = null, action: com.vaadin.ui.Button#ClickEvent => Unit = null, icon: Resource = null, style: String = null, enabled: Boolean = true, description:String = null) extends com.vaadin.ui.Button(caption) {
  setIcon(icon)
  setStyleName(style)
  setEnabled(enabled)

  Option(action) map addListener

  protected[vaadin] class ButtonClickListener(action: com.vaadin.ui.Button#ClickEvent => Unit) extends com.vaadin.ui.Button.ClickListener {
    def buttonClick(event: com.vaadin.ui.Button#ClickEvent) = action(event)
  }

  def addListener(action: com.vaadin.ui.Button#ClickEvent => Unit): Unit = addListener(new ButtonClickListener(action))
}

/**
 * Scala wrapper for com.vaadin.ui.Button with style Link */
class LinkButton(caption: String = null, action: com.vaadin.ui.Button#ClickEvent => Unit = null, icon: Resource = null, style: String = null, enabled: Boolean = true, description:String = null)
  extends Button(caption, action, icon, style, enabled, description) {
  setStyleName(BaseTheme.BUTTON_LINK)
}
}