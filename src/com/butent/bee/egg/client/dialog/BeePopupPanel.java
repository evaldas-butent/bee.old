package com.butent.bee.egg.client.dialog;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

import com.google.gwt.user.client.ui.PopupPanel;

public class BeePopupPanel extends PopupPanel implements HasId {

  public BeePopupPanel() {
    super(true);
    createId();
  }

  public BeePopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public BeePopupPanel(boolean autoHide) {
    super(autoHide);
    createId();
  }

  private void createId() {
    BeeDom.setId(this);
  }

  @Override
  public String getId() {
    return BeeDom.getId(this);
  }

  @Override
  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
