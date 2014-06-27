package com.butent.bee.client.view.form;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.ReadyEvent;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewHelper;

public class FormAndHeader extends Complex implements View {

  private Presenter viewPresenter;

  private boolean enabled = true;

  public FormAndHeader() {
    super();
  }

  @Override
  public HandlerRegistration addReadyHandler(ReadyEvent.Handler handler) {
    FormView form = getForm();
    if (form != null) {
      ViewHelper.delegateReadyEvent(this, form);
    }

    return addHandler(handler, ReadyEvent.getType());
  }

  public FormView getForm() {
    for (Widget child : this) {
      if (child instanceof FormView) {
        return (FormView) child;
      }
    }
    return null;
  }

  public HeaderView getHeader() {
    for (Widget child : this) {
      if (child instanceof HeaderView) {
        return (HeaderView) child;
      }
    }
    return null;
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled != isEnabled()) {
      this.enabled = enabled;
      DomUtils.enableChildren(this, enabled);
    }
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;

    for (Widget child : this) {
      if (child instanceof View && ((View) child).getViewPresenter() == null) {
        ((View) child).setViewPresenter(viewPresenter);
      }
    }
  }
}
