package com.butent.bee.shared.data;

import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;

/**
 * Contains necessary methods for row classes, for example {@code addCell} or {@code setValue}.
 */

public interface IsRow extends HasCustomProperties {
  void addCell(boolean value);

  void addCell(double value);

  void addCell(IsCell cell);

  void addCell(String value);

  void addCell(Value value);

  void clearCell(int index);

  IsRow clone();

  Boolean getBoolean(int index);

  IsCell getCell(int index);

  List<IsCell> getCells();

  JustDate getDate(int index);

  DateTime getDateTime(int index);

  Double getDouble(int index);

  long getId();

  int getNumberOfCells();

  String getString(int index);

  Value getValue(int index);
  Value getValue(int index, ValueType type);

  long getVersion();
  
  void insertCell(int index, IsCell cell);

  boolean isNull(int index);

  void removeCell(int index);

  void setCell(int index, IsCell cell);

  void setCells(List<IsCell> cells);

  void setValue(int index, boolean value);

  void setValue(int index, double value);

  void setValue(int index, String value);

  void setValue(int index, Value value);

  void setVersion(long version);  
}