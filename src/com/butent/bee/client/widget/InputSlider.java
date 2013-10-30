package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.shared.html.builder.elements.Input;
import com.butent.bee.shared.ui.EditorAction;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements a user interface component which lets to select a value by dragging a slider with a
 * mouse.
 */

public class InputSlider extends InputInteger {

  private static Input.Type inputType = Input.Type.RANGE;

  public InputSlider() {
    super();
    DomUtils.setInputType(this, inputType);
  }

  public InputSlider(int min, int max) {
    super(inputType, min, max);
  }

  public InputSlider(int min, int max, int step) {
    super(inputType, min, max, step);
  }

  public InputSlider(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  @Override
  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getIdPrefix() {
    return "slid";
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_SLIDER;
  }
  
  @Override
  public void setValue(String value) {
    if (BeeUtils.isEmpty(value) && BeeUtils.isInt(getMinValue())) {
      super.setValue(getMinValue());
    } else {
      super.setValue(value);
    }
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputSlider";
  }

  @Override
  protected boolean isTextBox() {
    return false;
  }
}
