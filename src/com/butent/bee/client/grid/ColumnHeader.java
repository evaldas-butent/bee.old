package com.butent.bee.client.grid;

import com.butent.bee.client.grid.cell.AbstractCell;
import com.butent.bee.client.grid.cell.HeaderCell;
import com.butent.bee.shared.ui.HasCaption;

/**
 * Creates new header cells for grids.
 */

public class ColumnHeader extends Header<String> implements HasCaption {
  
  private final String columnId;

  public ColumnHeader(String columnId, String caption) {
    this(columnId, new HeaderCell(caption));
  }

  public ColumnHeader(String columnId, AbstractCell<String> cell) {
    super(cell);
    this.columnId = columnId;
  }

  @Override
  public String getCaption() {
    if (getCell() instanceof HasCaption) {
      return ((HasCaption) getCell()).getCaption();
    } else {
      return null;
    }
  }

  @Override
  public String getValue() {
    return columnId;
  }
}
