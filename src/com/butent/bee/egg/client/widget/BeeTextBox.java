package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.TextBox;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.event.HasBeeKeyHandler;
import com.butent.bee.egg.client.event.HasBeeValueChangeHandler;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeTextBox extends TextBox implements HasId, HasBeeKeyHandler,
    HasBeeValueChangeHandler<String> {
  private String fieldName = null;

  public BeeTextBox() {
    super();
    createId();
    addDefaultHandlers();
  }

  public BeeTextBox(Element element) {
    super(element);
    createId();
    addDefaultHandlers();
  }

  public BeeTextBox(String fieldName) {
    this();
    this.fieldName = fieldName;

    String v = BeeGlobal.getFieldValue(fieldName);
    if (!BeeUtils.isEmpty(v)) {
      setValue(v);
    }
  }

  public void createId() {
    BeeDom.createId(this, "t");
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public boolean onBeeKey(KeyPressEvent event) {
    return true;
  }

  public boolean onValueChange(String value) {
    if (!BeeUtils.isEmpty(getFieldName())) {
      BeeGlobal.setFieldValue(getFieldName(), value);
    }

    return true;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  private void addDefaultHandlers() {
    BeeBus.addKeyHandler(this);
    BeeBus.addStringVch(this);
  }

}
