package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.FileUpload;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.event.HasBeeChangeHandler;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeFileUpload extends FileUpload implements HasId,
    HasBeeChangeHandler {
  private String fieldName = null;

  public BeeFileUpload() {
    super();
    createId();
    addDefaultHandlers();
  }

  public BeeFileUpload(Element element) {
    super(element);
    createId();
    addDefaultHandlers();
  }

  public BeeFileUpload(String fieldName) {
    this();
    this.fieldName = fieldName;
  }

  public void createId() {
    BeeDom.createId(this, "upload");
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public boolean onChange() {
    if (!BeeUtils.isEmpty(getFieldName())) {
      BeeGlobal.setFieldValue(getFieldName(), getFilename());
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
    BeeBus.addVch(this);
  }

}
