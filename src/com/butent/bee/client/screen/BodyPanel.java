package com.butent.bee.client.screen;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.style.StyleUtils;

public final class BodyPanel extends ComplexPanel {
  
  private static BodyPanel singleton;
  
  public static void conceal(Element el) {
    get().concealment.appendChild(el);
  }

  public static BodyPanel get() {
    if (singleton == null) {
      singleton = new BodyPanel();
    }
    return singleton;
  }
  
  private final Element concealment;
  
  private BodyPanel() {
    super();
    setElement(Document.get().getBody());
    addStyleName("bee-Body");
    
    this.concealment = Document.get().createDivElement();
    StyleUtils.makeAbsolute(concealment);
    StyleUtils.setTop(concealment, -1000);
    concealment.addClassName("bee-Concealment");
    
    getElement().appendChild(concealment);
    
    onAttach();
  }

  @Override
  public void add(Widget w) {
    add(w, getElement());
  }
}
