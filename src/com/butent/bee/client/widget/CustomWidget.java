package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.utils.BeeUtils;

public class CustomWidget extends Widget implements IdentifiableWidget, HasClickHandlers,
    HasMouseDownHandlers {

  public CustomWidget(Element element) {
    super();
    setElement(element);
    init();
  }

  public CustomWidget(Element element, String styleName) {
    this(element);
    if (!BeeUtils.isEmpty(styleName)) {
      addStyleName(styleName);
    }
  }

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }

  @Override
  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
    return addDomHandler(handler, MouseDownEvent.getType());
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "custom";
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  protected void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
