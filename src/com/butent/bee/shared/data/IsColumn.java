package com.butent.bee.shared.data;

import com.butent.bee.shared.data.value.ValueType;

public interface IsColumn extends HasCustomProperties {
  IsColumn clone();
  
  String getId();
  String getLabel();
  String getPattern();

  ValueType getType();

  void setId(String id);
  void setLabel(String label);
  void setPattern(String pattern);

  void setType(ValueType type);
}