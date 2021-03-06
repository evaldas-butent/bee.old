package com.butent.bee.server.rest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.exceptions.BeeException;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(RestResponse.JSON_TYPE)
public abstract class CrudWorker {

  static final String ID = "ID";
  static final String VERSION = "VERSION";

  static final String OLD_VALUES = "OLD_VALUES";
  private static final String CONTACT_VERSION = "ContactVersion";

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @Resource
  UserTransaction utx;

  @DELETE
  @Path("{" + ID + ":\\d+}/{" + VERSION + ":\\d+}")
  public RestResponse delete(@PathParam(ID) Long id, @PathParam(VERSION) Long version) {
    if (!usr.canDeleteData(getViewName())) {
      return RestResponse.forbidden();
    }
    RestResponse response = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          ResponseObject resp = deb.deleteRows(getViewName(),
              new RowInfo[] {new RowInfo(id, version)});

          if (resp.hasErrors()) {
            throw new BeeRuntimeException(ArrayUtils.joinWords(resp.getErrors()));
          }
        }
      });
    } catch (BeeException e) {
      response = RestResponse.error(e);
    }
    if (Objects.isNull(response)) {
      response = RestResponse.empty();
    } else {
      response.setResult(get(id).getResult());
    }
    return response;
  }

  @GET
  @Path("{" + ID + ":\\d+}")
  public RestResponse get(@PathParam(ID) Long id) {
    return get(Filter.compareId(id));
  }

  public RestResponse get(Filter filter) {
    long time = System.currentTimeMillis();
    BeeRowSet rowSet = qs.getViewData(getViewName(), filter);
    return RestResponse.ok(getData(rowSet, null)).setLastSync(time);
  }

  @GET
  public RestResponse getAll(@HeaderParam(RestResponse.LAST_SYNC_TIME) Long lastSynced) {
    long sync = BeeUtils.unbox(lastSynced);
    Filter filter = Filter.compareVersion(Operator.GT, sync);

    if (sys.getView(getViewName()).hasColumn(CONTACT_VERSION)) {
      filter = Filter.or(filter, Filter.isMore(CONTACT_VERSION, Value.getValue(sync)));
    }
    return get(filter);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public RestResponse insert(JsonObject data) {
    Holder<Long> idHolder = Holder.of(BeeUtils.toLongOrNull(getValue(data, ID)));

    if (DataUtils.isId(idHolder.get())) {
      return update(idHolder.get(), BeeUtils.toLongOrNull(getValue(data, VERSION)), data);
    }
    if (!usr.canCreateData(getViewName())) {
      return RestResponse.forbidden();
    }
    RestResponse response = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          idHolder.set(insert(getViewName(), data));
        }
      });
    } catch (BeeException e) {
      response = RestResponse.error(e);
    }
    if (Objects.isNull(response)) {
      response = get(idHolder.get());
    }
    return response;
  }

  @PUT
  @Path("{" + ID + ":\\d+}/{" + VERSION + ":\\d+}")
  @Consumes(MediaType.APPLICATION_JSON)
  public RestResponse update(@PathParam(ID) Long id, @PathParam(VERSION) Long version,
      JsonObject data) {

    if (!usr.canEditData(getViewName())) {
      return RestResponse.forbidden();
    }
    RestResponse error = null;

    try {
      commit(new Runnable() {
        @Override
        public void run() {
          update(getViewName(), id, version, data);
        }
      });
    } catch (BeeException e) {
      error = RestResponse.error(e);
    }
    RestResponse response = get(id);

    if (Objects.nonNull(error)) {
      response = error.setResult(response.getResult());
    }
    return response;
  }

  public static String getValue(JsonObject data, String field) {
    String value = null;
    JsonValue object = data.get(field);

    if (Objects.nonNull(object) && object != JsonValue.NULL) {
      value = object instanceof JsonString ? ((JsonString) object).getString() : object.toString();
    }
    return value;
  }

  public static Object getValue(BeeRowSet rowSet, int row, int col) {
    Object value = null;

    switch (rowSet.getColumnType(col)) {
      case BOOLEAN:
        value = rowSet.getBoolean(row, col);
        break;
      case DATE:
      case DATE_TIME:
      case INTEGER:
      case LONG:
        value = rowSet.getLong(row, col);
        break;
      case DECIMAL:
      case NUMBER:
        value = rowSet.getDecimal(row, col);
        break;
      default:
        value = rowSet.getString(row, col);
        break;
    }
    return value;
  }

  void commit(Runnable executor) throws BeeException {
    try {
      utx.begin();
      executor.run();
      utx.commit();
    } catch (Throwable ex) {
      try {
        utx.rollback();
      } catch (Throwable ex2) {
      }
      throw BeeException.error(ex);
    }
  }

  static Collection<Map<String, Object>> getData(BeeRowSet rs, Multimap<String, String> children) {
    Map<Long, Map<String, Object>> data = new LinkedHashMap<>();
    boolean hasChildren = Objects.nonNull(children);
    Multimap<String, Map<String, Object>> childData = HashMultimap.create();

    for (int i = 0; i < rs.getNumberOfRows(); i++) {
      BeeRow beeRow = rs.getRow(i);

      if (!data.containsKey(beeRow.getId())) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(ID, beeRow.getId());
        row.put(VERSION, beeRow.getVersion());

        for (int j = 0; j < rs.getNumberOfColumns(); j++) {
          String col = rs.getColumnId(j);

          if (!hasChildren || !children.containsValue(col)) {
            row.put(col, getValue(rs, i, j));
          }
        }
        data.put(beeRow.getId(), row);
      }
      if (hasChildren) {
        for (String child : children.keySet()) {
          Map<String, Object> row = new LinkedHashMap<>();

          for (String col : children.get(child)) {
            int j = rs.getColumnIndex(col);

            if (!BeeConst.isUndef(j)) {
              row.put(col, getValue(rs, i, j));
            }
          }
          if (BeeUtils.anyNotNull(row.values())) {
            String key = BeeUtils.joinWords(beeRow.getId(), child);

            if (!childData.containsEntry(key, row)) {
              childData.put(key, row);
            }
          }
        }
      }
    }
    if (hasChildren) {
      for (Long id : data.keySet()) {
        for (String child : children.keySet()) {
          data.get(id).put(child, childData.get(BeeUtils.joinWords(id, child)));
        }
      }
    }
    return data.values();
  }

  abstract String getViewName();

  private static BeeColumn getParentColumn(BeeView view, String parentCol) {
    BeeColumn parent = null;

    for (ViewColumn viewColumn : view.getViewColumns()) {
      if (!Objects.equals(viewColumn.getName(), parentCol)
          && Objects.equals(viewColumn.getTable(), view.getColumnTable(parentCol))
          && Objects.equals(viewColumn.getField(), view.getColumnField(parentCol))
          && Objects.equals(viewColumn.getParent(), view.getColumnParent(parentCol))) {

        parent = view.getBeeColumn(viewColumn.getName());
        break;
      }
    }
    if (Objects.isNull(parent)) {
      parent = view.getBeeColumn(parentCol);
    }
    return parent;
  }

  private String getRelation(BeeView view, String parentCol, JsonObject data) {
    String value = getValue(data, getParentColumn(view, parentCol).getId());

    if (DataUtils.isId(value)) {
      return value;
    }
    Map<String, Object> fields = new HashMap<>();

    for (ViewColumn viewColumn : view.getViewColumns()) {
      if (Objects.equals(viewColumn.getParent(), parentCol)
          && data.containsKey(viewColumn.getName())) {

        fields.put(viewColumn.getField(), getValue(data, viewColumn.getName()));
      }
    }
    if (Objects.equals(view.getColumnRelation(parentCol), ClassifierConstants.TBL_CITIES)) {
      String parent = view.getColumnParent(parentCol);

      for (ViewColumn viewColumn : view.getViewColumns()) {
        if (Objects.equals(viewColumn.getParent(), parent)
            && Objects.equals(viewColumn.getRelation(), ClassifierConstants.TBL_COUNTRIES)
            && viewColumn.isHidden()) {

          fields.put(ClassifierConstants.COL_COUNTRY,
              BeeUtils.toLongOrNull(getRelation(view, viewColumn.getName(), data)));
          break;
        }
      }
    }
    boolean ok = false;

    for (Object o : fields.values()) {
      if (Objects.nonNull(o)) {
        ok = true;
        break;
      }
    }
    if (!ok) {
      return null;
    }
    String table = view.getColumnRelation(parentCol);
    HasConditions clause = SqlUtils.and();

    for (Map.Entry<String, Object> entry : fields.entrySet()) {
      if (entry.getValue() instanceof String) {
        clause.add(SqlUtils.same(table, entry.getKey(), (String) entry.getValue()));
      } else {
        clause.add(SqlUtils.equals(table, entry.getKey(), entry.getValue()));
      }
    }
    String[] ids = qs.getColumn(new SqlSelect()
        .addFields(table, sys.getIdName(table))
        .addFrom(table)
        .setWhere(clause));

    String id = (ids.length == 1) ? ids[0] : null;

    if (!DataUtils.isId(id)) {
      SqlInsert insert = new SqlInsert(table);

      for (Map.Entry<String, Object> entry : fields.entrySet()) {
        insert.addNotNull(entry.getKey(), entry.getValue());
      }
      ResponseObject response = qs.insertDataWithResponse(insert);

      if (response.hasErrors()) {
        throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
      }
      id = BeeUtils.toString(response.getResponseAsLong());
    }
    return id;
  }

  private Long insert(String viewName, JsonObject data) {
    LogUtils.getRootLogger().debug(viewName, data);

    BeeView view = sys.getView(viewName);
    Map<String, BeeColumn> columns = new LinkedHashMap<>();
    List<String> values = new ArrayList<>();

    for (String col : data.keySet()) {
      if (view.hasColumn(col)) {
        BeeColumn column = view.getBeeColumn(col);
        String value = getValue(data, col);

        if (isReadOnly(column) || columns.containsKey(column.getId()) || BeeUtils.isEmpty(value)) {
          continue;
        }
        if (column.isForeign()) {
          String parentCol = view.getColumnParent(col);
          column = getParentColumn(view, parentCol);

          if (columns.containsKey(column.getId())) {
            continue;
          }
          value = getRelation(view, parentCol, data);
        }
        if (usr.canEditColumn(getViewName(), column.getId())) {
          columns.put(column.getId(), column);
          values.add(value);
        }
      }
    }
    BeeRowSet rs = DataUtils.createRowSetForInsert(viewName, new ArrayList<>(columns.values()),
        values);

    if (Objects.isNull(rs)) {
      throw new BeeRuntimeException(Localized.dictionary().noData());
    }
    ResponseObject response = deb.commitRow(rs, RowInfo.class);

    if (response.hasErrors()) {
      throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
    }
    return ((RowInfo) response.getResponse()).getId();
  }

  private static boolean isReadOnly(BeeColumn column) {
    return column.isReadOnly() || column.getLevel() > 1;
  }

  private BeeRowSet update(String viewName, Long id, Long version, JsonObject data) {
    LogUtils.getRootLogger().debug(data);

    if (data.containsKey(OLD_VALUES) && data.get(OLD_VALUES) instanceof JsonObject) {
      JsonObject oldData = data.getJsonObject(OLD_VALUES);
      BeeView view = sys.getView(viewName);
      Map<String, BeeColumn> columns = new LinkedHashMap<>();
      List<String> oldValues = new ArrayList<>();
      List<String> newValues = new ArrayList<>();

      for (String col : oldData.keySet()) {
        if (view.hasColumn(col)) {
          BeeColumn column = view.getBeeColumn(col);

          if (isReadOnly(column) || columns.containsKey(column.getId())) {
            continue;
          }
          String oldValue;
          String value;

          if (column.isForeign()) {
            String parentCol = view.getColumnParent(col);
            column = getParentColumn(view, parentCol);

            String key = getValue(data, column.getId());

            if (!DataUtils.isId(key) || oldData.containsKey(column.getId())) {
              value = getRelation(view, parentCol, data);

              if (columns.containsKey(column.getId())) {
                newValues.set(new ArrayList<>(columns.keySet()).indexOf(column.getId()), value);
                continue;
              }
              oldValue = getRelation(view, parentCol, oldData);
            } else {
              String table = view.getColumnRelation(column.getId());

              ResponseObject response = qs.updateDataWithResponse(new SqlUpdate(table)
                  .addConstant(view.getColumnField(col), getValue(data, col))
                  .setWhere(sys.idEquals(table, BeeUtils.toLong(key))));

              if (response.hasErrors()) {
                throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
              }
              continue;
            }
          } else {
            oldValue = getValue(oldData, col);
            value = getValue(data, col);
          }
          if (usr.canEditColumn(getViewName(), column.getId())) {
            columns.put(column.getId(), column);
            oldValues.add(oldValue);
            newValues.add(value);
          }
        }
      }
      BeeRowSet rs = DataUtils.getUpdated(viewName, id, version,
          new ArrayList<>(columns.values()), oldValues, newValues, null);

      if (Objects.nonNull(rs)) {
        ResponseObject response = deb.commitRow(rs, RowInfo.class);

        if (response.hasErrors()) {
          throw new BeeRuntimeException(ArrayUtils.joinWords(response.getErrors()));
        }
      }
      return rs;
    }
    return null;
  }
}
