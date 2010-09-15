package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;

public class UiMenuHorizontal extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createMenuHorizontal(this);
  }
}
