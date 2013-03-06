package com.butent.bee.shared.data.view;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.cache.ReplacementPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Enables to get main information about data objects, like row count, ID column etc.
 */

public class DataInfo implements BeeSerializable, Comparable<DataInfo>, HasExtendedInfo,
    HasViewName {

  public interface Provider {
    DataInfo getDataInfo(String viewName, boolean warn);
  }

  public static DataInfo restore(String s) {
    Assert.notEmpty(s);
    DataInfo ti = new DataInfo();
    ti.deserialize(s);
    return ti;
  }

  private String viewName;
  private String tableName;

  private String idColumn;
  private String versionColumn;

  private String caption;

  private String editForm;
  private String rowCaption;

  private String newRowForm;
  private String newRowColumns;
  private String newRowCaption;

  private Integer cacheMaximumSize;
  private String cacheEviction;
  
  private final List<BeeColumn> columns = Lists.newArrayList();
  private final List<ViewColumn> viewColumns = Lists.newArrayList();

  private int rowCount = BeeConst.UNDEF;

  public DataInfo(String viewName, String tableName, String idColumn, String versionColumn,
      String caption, String editForm, String rowCaption,
      String newRowForm, String newRowColumns, String newRowCaption,
      Integer cacheMaximumSize, String cacheEviction,
      List<BeeColumn> columns, List<ViewColumn> viewColumns) {

    setViewName(viewName);
    setTableName(tableName);

    setIdColumn(idColumn);
    setVersionColumn(versionColumn);
    
    setCaption(caption);

    setEditForm(editForm);
    setRowCaption(rowCaption);
    
    setNewRowForm(newRowForm);
    setNewRowColumns(newRowColumns);
    setNewRowCaption(newRowCaption);
    
    setCacheMaximumSize(cacheMaximumSize);
    setCacheEviction(cacheEviction);

    if (columns != null) {
      this.columns.addAll(columns);
    }
    if (viewColumns != null) {
      this.viewColumns.addAll(viewColumns);
    }
  }

  private DataInfo() {
  }

  public int compareTo(DataInfo o) {
    if (o == null) {
      return BeeConst.COMPARE_MORE;
    }
    return BeeUtils.compare(getViewName(), o.getViewName());
  }

  public boolean containsAllViewColumns(Collection<String> columnNames) {
    if (BeeUtils.isEmpty(columnNames)) {
      return false;
    }

    for (String colName : columnNames) {
      if (!containsViewColumn(colName)) {
        return false;
      }
    }
    return true;
  }

  public boolean containsColumn(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return false;
    } else {
      return !BeeConst.isUndef(getColumnIndex(columnId));
    }
  }

  public boolean containsViewColumn(String colName) {
    if (BeeUtils.isEmpty(colName)) {
      return false;
    } else {
      return getViewColumn(colName) != null;
    }
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 15);
    int index = 0;

    setViewName(arr[index++]);
    setTableName(arr[index++]);
    setIdColumn(arr[index++]);
    setVersionColumn(arr[index++]);

    setCaption(arr[index++]);

    setEditForm(arr[index++]);
    setRowCaption(arr[index++]);

    setNewRowForm(arr[index++]);
    setNewRowColumns(arr[index++]);
    setNewRowCaption(arr[index++]);
    
    setCacheMaximumSize(BeeUtils.toIntOrNull(arr[index++]));
    setCacheEviction(arr[index++]);

    getColumns().clear();
    String[] cArr = Codec.beeDeserializeCollection(arr[index++]);
    if (!ArrayUtils.isEmpty(cArr)) {
      for (String col : cArr) {
        getColumns().add(BeeColumn.restore(col));
      }
    }

    getViewColumns().clear();
    cArr = Codec.beeDeserializeCollection(arr[index++]);
    if (!ArrayUtils.isEmpty(cArr)) {
      for (String col : cArr) {
        getViewColumns().add(ViewColumn.restore(col));
      }
    }

    setRowCount(BeeUtils.toInt(arr[index++]));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DataInfo)) {
      return false;
    }
    return BeeUtils.same(getViewName(), ((DataInfo) obj).getViewName());
  }

  public Integer getCacheMaximumSize() {
    return cacheMaximumSize;
  }

  public ReplacementPolicy getCacheReplacementPolicy() {
    return NameUtils.getEnumByName(ReplacementPolicy.class, getCacheEviction());
  }

  public String getCaption() {
    return caption;
  }

  public BeeColumn getColumn(String columnId) {
    int index = getColumnIndex(columnId);
    return BeeConst.isUndef(index) ? null : getColumns().get(index);
  }

  public int getColumnCount() {
    return (getColumns() == null) ? BeeConst.UNDEF : getColumns().size();
  }

  public String getColumnId(int index) {
    Assert.isIndex(getColumns(), index);
    return getColumns().get(index).getId();
  }

  public int getColumnIndex(String columnId) {
    if (BeeUtils.isEmpty(columnId)) {
      return BeeConst.UNDEF;
    } else if (BeeUtils.same(columnId, getIdColumn())) {
      return DataUtils.ID_INDEX;
    } else if (BeeUtils.same(columnId, getVersionColumn())) {
      return DataUtils.VERSION_INDEX;
    } else {
      return DataUtils.getColumnIndex(columnId, getColumns());
    }
  }

  public int getColumnIndexBySource(String table, String field, int preferredLevel) {
    int index = getColumnIndexBySource(table, field, ViewColumn.Level.of(preferredLevel));
    if (BeeConst.isUndef(index)) {
      index = getColumnIndexBySource(table, field, null);
    }

    return index;
  }

  public int getColumnIndexBySource(String table, String field, Predicate<ViewColumn> predicate) {
    int index = BeeConst.UNDEF;
    List<ViewColumn> vcs = getViewColumnsBySource(table, field, predicate);
    if (vcs.isEmpty()) {
      return index;
    }

    for (ViewColumn vc : vcs) {
      index = getColumnIndex(vc.getName());
      if (!BeeConst.isUndef(index)) {
        break;
      }
    }
    return index;
  }

  public List<String> getColumnNames(boolean includeIdAndVersion) {
    if (includeIdAndVersion) {
      return DataUtils.getColumnNames(getColumns(), getIdColumn(), getVersionColumn());
    } else {
      return DataUtils.getColumnNames(getColumns());
    }
  }

  public Integer getColumnPrecision(String columnId) {
    BeeColumn column = getColumn(columnId);
    return (column == null) ? null : column.getPrecision();
  }
  
  public List<BeeColumn> getColumns() {
    return columns;
  }

  public ValueType getColumnType(int index) {
    if (index == DataUtils.ID_INDEX) {
      return DataUtils.ID_TYPE;
    } else if (index == DataUtils.VERSION_INDEX) {
      return DataUtils.VERSION_TYPE;
    } else if (isColumnIndex(index)) {
      return getColumns().get(index).getType();
    } else {
      return null;
    }
  }
  
  public ValueType getColumnType(String columnId) {
    BeeColumn column = getColumn(columnId);
    return (column == null) ? null : column.getType();
  }
  
  public Collection<ViewColumn> getDescendants(String colName, boolean includeHidden) {
    Set<ViewColumn> result = Sets.newHashSet();

    ViewColumn root = getViewColumn(colName);
    if (root == null) {
      return result;
    }

    String table = root.getTable();
    String field = root.getField();
    String parent = root.getParent();

    if (BeeUtils.anyEmpty(table, field)) {
      return result;
    }

    Set<String> parents = Sets.newHashSet(root.getName());
    for (ViewColumn vc : getViewColumns()) {
      if (BeeUtils.same(vc.getField(), field)
          && BeeUtils.same(vc.getTable(), table)
          && BeeUtils.same(vc.getParent(), parent)
          && !BeeUtils.same(vc.getName(), root.getName())) {
        parents.add(vc.getName());
      }
    }

    while (!parents.isEmpty()) {
      Set<ViewColumn> children = Sets.newHashSet();
      for (String p : parents) {
        children.addAll(getImmediateChildren(p));
      }
      if (children.isEmpty()) {
        break;
      }

      if (includeHidden) {
        result.addAll(children);
      } else {
        for (ViewColumn child : children) {
          if (!child.isHidden()) {
            result.add(child);
          }
        }
      }

      parents.clear();
      for (ViewColumn vc : children) {
        parents.add(vc.getName());
      }
    }
    return result;
  }

  public String getEditableRelationSource(String colName) {
    ViewColumn viewColumn = getViewColumn(colName);
    if (viewColumn == null) {
      return null;
    }

    if (hasEditableRelation(viewColumn)) {
      if (containsColumn(viewColumn.getName())) {
        return viewColumn.getName();
      }
      if (containsColumn(viewColumn.getField())) {
        return viewColumn.getField();
      }
    }

    if (!BeeUtils.isEmpty(viewColumn.getParent())) {
      return getEditableRelationSource(viewColumn.getParent());
    } else {
      return null;
    }
  }

  public String getEditableRelationView(String colName) {
    ViewColumn viewColumn = getViewColumn(colName);

    if (viewColumn == null) {
      return null;
    } else if (hasEditableRelation(viewColumn)) {
      return viewColumn.getRelation();
    } else if (!BeeUtils.isEmpty(viewColumn.getParent())) {
      return getEditableRelationView(viewColumn.getParent());
    } else {
      return null;
    }
  }

  public String getEditForm() {
    return editForm;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> result = Lists.newArrayList();
    PropertyUtils.addProperties(result, false,
        "View Name", getViewName(),
        "Table Name", getTableName(),
        "Id Column", getIdColumn(),
        "Version Column", getVersionColumn(),
        "Caption", getCaption(),
        "Edit Form", getEditForm(),
        "Row Caption", getRowCaption(),
        "New Row Form", getNewRowForm(),
        "New Row Columns", getNewRowColumns(),
        "New Row Caption", getNewRowCaption(),
        "Row Count", getRowCount(),
        "Cache Maximum Size", getCacheMaximumSize(),
        "Cache Eviction", getCacheEviction(),
        "Column Count", getColumnCount());

    int cc = getColumnCount();
    for (int i = 0; i < cc; i++) {
      BeeColumn column = getColumns().get(i);
      PropertyUtils.appendChildrenToExtended(result, BeeUtils.progress(i + 1, cc, column.getId()),
          column.getInfo());
    }

    cc = getViewColumns().size();
    PropertyUtils.addExtended(result, "View Columns", BeeUtils.bracket(cc));
    for (int i = 0; i < cc; i++) {
      ViewColumn column = getViewColumns().get(i);
      PropertyUtils.appendChildrenToExtended(result,
          BeeUtils.progress(i + 1, cc, column.getName()),
          column.getInfo());
    }

    return result;
  }

  public String getField(String colName) {
    ViewColumn viewColumn = getViewColumn(colName);
    return (viewColumn == null) ? null : viewColumn.getField();
  }

  public String getIdColumn() {
    return idColumn;
  }

  public Collection<ViewColumn> getImmediateChildren(String parent) {
    Set<ViewColumn> children = Sets.newHashSet();
    if (BeeUtils.isEmpty(parent)) {
      return children;
    }

    for (ViewColumn vc : getViewColumns()) {
      if (BeeUtils.same(vc.getParent(), parent)) {
        children.add(vc);
      }
    }
    return children;
  }

  public String getNewRowCaption() {
    return newRowCaption;
  }

  public String getNewRowColumns() {
    return newRowColumns;
  }

  public String getNewRowForm() {
    return newRowForm;
  }

  public List<String> getRelatedTables() {
    List<String> tables = Lists.newArrayList();

    for (ViewColumn viewColumn : getViewColumns()) {
      if (viewColumn.isHidden() || viewColumn.isReadOnly()) {
        continue;
      }

      String table = (viewColumn.getLevel() > 0) ? viewColumn.getTable() : viewColumn.getRelation();
      if (!BeeUtils.isEmpty(table) && !tables.contains(table)) {
        tables.add(table);
      }
    }
    return tables;
  }

  public String getRelationView(String colName) {
    ViewColumn viewColumn = getViewColumn(colName);

    if (viewColumn == null) {
      return null;
    } else if (!BeeUtils.isEmpty(viewColumn.getRelation())) {
      return viewColumn.getRelation();
    } else if (!BeeUtils.isEmpty(viewColumn.getParent())) {
      return getRelationView(viewColumn.getParent());
    } else {
      return null;
    }
  }

  public String getRowCaption() {
    return rowCaption;
  }

  public int getRowCount() {
    return rowCount;
  }

  public String getTableName() {
    return tableName;
  }

  public String getVersionColumn() {
    return versionColumn;
  }

  public ViewColumn getViewColumn(String colName) {
    for (ViewColumn vc : getViewColumns()) {
      if (BeeUtils.same(colName, vc.getName())) {
        return vc;
      }
    }
    return null;
  }

  public List<ViewColumn> getViewColumns() {
    return viewColumns;
  }

  public List<ViewColumn> getViewColumnsBySource(String table, String field,
      Predicate<ViewColumn> predicate) {
    List<ViewColumn> result = Lists.newArrayList();
    if (BeeUtils.anyEmpty(table, field)) {
      return result;
    }

    for (ViewColumn vc : getViewColumns()) {
      if (BeeUtils.same(table, vc.getTable()) && BeeUtils.same(field, vc.getField())) {
        if (BeeUtils.check(predicate, vc)) {
          result.add(vc);
        }
      }
    }
    return result;
  }

  public String getViewName() {
    return viewName;
  }

  @Override
  public int hashCode() {
    return BeeUtils.normalize(getViewName()).hashCode();
  }

  public boolean hasRelation(String colName) {
    ViewColumn viewColumn = getViewColumn(colName);
    return (viewColumn != null && !BeeUtils.isEmpty(viewColumn.getRelation()));
  }

  public boolean isColumnIndex(int index) {
    return BeeUtils.isIndex(getColumns(), index);
  }

  public List<String> parseColumns(List<String> input) {
    return DataUtils.parseColumns(input, getColumns(), getIdColumn(), getVersionColumn());
  }

  public List<String> parseColumns(String input) {
    return DataUtils.parseColumns(input, getColumns(), getIdColumn(), getVersionColumn());
  }

  public Filter parseFilter(String input) {
    return DataUtils.parseCondition(input, getColumns(), getIdColumn(), getVersionColumn());
  }

  public Order parseOrder(String input) {
    return Order.parse(input, getColumnNames(true));
  }

  public String serialize() {
    return Codec.beeSerialize(
        new Object[] {getViewName(), getTableName(), getIdColumn(), getVersionColumn(),
            getCaption(), getEditForm(), getRowCaption(),
            getNewRowForm(), getNewRowColumns(), getNewRowCaption(),
            getCacheMaximumSize(), getCacheEviction(),
            getColumns(), getViewColumns(), getRowCount()});
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }
  
  private String getCacheEviction() {
    return cacheEviction;
  }
  
  private boolean hasEditableRelation(ViewColumn viewColumn) {
    if (viewColumn == null) {
      return false;
    } else {
      return !BeeUtils.isEmpty(viewColumn.getRelation())
          && (viewColumn.getLevel() <= 0 || BeeUtils.isTrue(viewColumn.getEditable()));
    }
  }

  private void setCacheEviction(String cacheEviction) {
    this.cacheEviction = cacheEviction;
  }

  private void setCacheMaximumSize(Integer cacheMaximumSize) {
    this.cacheMaximumSize = cacheMaximumSize;
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setEditForm(String editForm) {
    this.editForm = editForm;
  }

  private void setIdColumn(String idColumn) {
    this.idColumn = idColumn;
  }

  private void setNewRowCaption(String newRowCaption) {
    this.newRowCaption = newRowCaption;
  }

  private void setNewRowColumns(String newRowColumns) {
    this.newRowColumns = newRowColumns;
  }
  
  private void setNewRowForm(String newRowForm) {
    this.newRowForm = newRowForm;
  }

  private void setRowCaption(String rowCaption) {
    this.rowCaption = rowCaption;
  }

  private void setTableName(String tableName) {
    this.tableName = tableName;
  }

  private void setVersionColumn(String versionColumn) {
    this.versionColumn = versionColumn;
  }

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }
}
