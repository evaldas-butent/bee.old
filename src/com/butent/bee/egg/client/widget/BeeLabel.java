package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.egg.client.BeeBus;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeLabel extends Label implements HasId, HasCommand {
  private BeeCommand command = null;

  public BeeLabel() {
    super();
    createId();
  }

  public BeeLabel(Element element) {
    super(element);
    createId();
  }

  public BeeLabel(Object obj) {
    this(BeeUtils.transform(obj));
  }

  public BeeLabel(String text) {
    super(text);
    createId();
  }

  public BeeLabel(String text, BeeCommand cmnd) {
    this(text);

    if (cmnd != null) {
      setCommand(cmnd);
      BeeBus.addClickHandler(this);
    }
  }

  public BeeLabel(String text, boolean wordWrap) {
    super(text, wordWrap);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "l");
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
