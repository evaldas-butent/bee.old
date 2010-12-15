package com.butent.bee.egg.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.Global;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.grid.model.CachedTableModel;
import com.butent.bee.egg.client.grid.model.TableModel;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Request;
import com.butent.bee.egg.client.grid.model.TableModelHelper.Response;
import com.butent.bee.egg.client.grid.render.FixedWidthGridBulkRenderer;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.HasTabularData;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GridFactory {

  private class ScrollGridColumnDefinition extends ColumnDefinition<Integer, String> {
    private HasTabularData view;
    private int idx;
    private int maxDisplaySize;

    private ScrollGridColumnDefinition(HasTabularData view, int idx) {
      this(view, idx, -1);
    }

    private ScrollGridColumnDefinition(HasTabularData view, int idx, int max) {
      this.view = view;
      this.idx = idx;
      this.maxDisplaySize = max;
      
      setColumnId(idx);
      setColumnOrder(idx);
    }

    @Override
    public String getCellValue(Integer rowValue) {
      String v = view.getValue(rowValue, idx);
      if (v == null) {
        return BeeConst.STRING_EMPTY;
      }
      if (maxDisplaySize <= 0 || v.length() <= maxDisplaySize) {
        return v;
      }

      return BeeUtils.clip(v, maxDisplaySize);
    }

    @Override
    public void setCellValue(Integer rowValue, String cellValue) {
      view.setValue(rowValue, idx, cellValue);
    }
  }

  private class ScrollGridResponse extends Response<Integer> {
    private Collection<Integer> rowValues = new ArrayList<Integer>();

    private ScrollGridResponse(int start, int cnt, int tot) {
      for (int i = start; i < start + cnt && i < tot; i++) {
        this.rowValues.add(i);
      }
    }

    @Override
    public Iterator<Integer> getRowValues() {
      return rowValues.iterator();
    }
  }

  private class ScrollGridTableModel extends TableModel<Integer> {
    public void requestRows(Request request, TableModel.Callback<Integer> callback) {
      int start = request.getStartRow();
      int cnt = request.getNumRows();

      ScrollGridResponse resp = new ScrollGridResponse(start, cnt, getRowCount());
      callback.onRowsReady(request, resp);
    }
  }

  public Widget cellGrid(Object data, CellType cellType, Object... columns) {
    Assert.notNull(data);

    HasTabularData view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    String table = null;
    CellKeyProvider keyProvider = null;

    if (!(view instanceof BeeRowSet) && BeeUtils.arrayLength(view.getColumns()) > 0) {
      table = view.getColumns()[0].getTable();
    }

    if (!BeeUtils.isEmpty(table)) {
      keyProvider = new CellKeyProvider(view);
      Global.getCache().getPrimaryKey(table, keyProvider);
    }

    BeeCellTable cellTable = new BeeCellTable(r, keyProvider);

    TextColumn column;
    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      column = new TextColumn(createCell(cellType), view, i);
      if (cellType != null && cellType.isEditable()) {
        column.setFieldUpdater(new CellUpdater(view, i, keyProvider));
      }
      cellTable.addColumn(column, arr[i]);
    }
    cellTable.initData(r);

    return cellTable;
  }

  public Widget scrollGrid(int width, Object data, Object... columns) {
    Assert.notNull(data);

    HasTabularData view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    ScrollGridTableModel tableModel = new ScrollGridTableModel();
    CachedTableModel<Integer> cachedModel = new CachedTableModel<Integer>(tableModel);
    cachedModel.setRowCount(r);

    TableDefinition<Integer> tableDef = new TableDefinition<Integer>();

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      ScrollGridColumnDefinition colDef = new ScrollGridColumnDefinition(view, i, 512);
      colDef.setHeader(arr[i]);
      colDef.setFooter("col " + i);

      tableDef.addColumnDefinition(colDef);
    }

    ScrollTable<Integer> table = new ScrollTable<Integer>(cachedModel, tableDef);
    if (width > c) {
      int w = (width - DomUtils.getScrollbarWidth() - 2) / c;
      table.setDefaultColumnWidth(BeeUtils.limit(w, 60, 300));
    }
    table.createFooterTable();

    FixedWidthGridBulkRenderer<Integer> renderer = 
      new FixedWidthGridBulkRenderer<Integer>(table.getDataTable(), table);
    table.setBulkRenderer(renderer);

    return table;
  }

  public Widget simpleGrid(Object data, Object... columns) {
    Assert.notNull(data);

    HasTabularData view = DataUtils.createView(data, columns);
    Assert.notNull(view);

    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    BeeCellTable table = new BeeCellTable(r);

    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      table.addColumn(new TextColumn(createCell(CellType.TEXT), view, i, 256), arr[i]);
    }
    table.initData(r);

    return table;
  }

  private Cell<String> createCell(CellType type) {
    Cell<String> cell;

    switch (type) {
      case TEXT_EDIT:
        cell = new EditTextCell();
        break;
      case TEXT_INPUT:
        cell = new TextInputCell();
        break;
      default:
        cell = new TextCell();
    }

    return cell;
  }
}