package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Callback;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcParameter;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;

/**
 * Contains methods for getting {@code RowSets} and making POST requests.
 */

public class Queries {

  private static final int RESPONSE_FROM_CACHE = 0;

  /**
   * Requires implementing classes to have {@code onResponse) method. 
   */
  public interface IntCallback extends Callback<Integer, String[]> {
  }

  /**
   * Contains requirements for row callback managing classes.
   */

  public interface RowCallback extends Callback<BeeRow, String[]> {
  }

  /**
   * Requires implementing classes to have {@code onResponse) method applied for a {@code RowSet}
   * object.
   */
  public interface RowSetCallback extends Callback<BeeRowSet, String[]> {
  }

  /**
   * Contains requirements for timestamp changes callback managing classes.
   */

  public interface VersionCallback extends Callback<Long, String[]> {
  }

  public static void deleteRow(String viewName, long rowId, long version) {
    deleteRow(viewName, rowId, version, null);
  }

  public static void deleteRow(String viewName, long rowId, long version, IntCallback callback) {
    deleteRows(viewName, Lists.newArrayList(new RowInfo(rowId, version)), callback);
  }

  public static void deleteRows(String viewName, Collection<RowInfo> rows) {
    deleteRows(viewName, rows, null);
  }

  public static void deleteRows(final String viewName, Collection<RowInfo> rows,
      final IntCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(rows);

    final int requestCount = rows.size();
    Assert.isPositive(requestCount);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName,
        Service.VAR_VIEW_ROWS, Codec.beeSerialize(rows));

    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.DELETE_ROWS,
        RpcParameter.SECTION.DATA, lst),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);
            String s = (String) response.getResponse();

            if (BeeUtils.isInt(s)) {
              int responseCount = BeeUtils.toInt(s);
              String message;
              if (responseCount == requestCount) {
                message = BeeUtils.concat(1, viewName, "deleted", responseCount, "rows");
                BeeKeeper.getLog().info(message);
              } else {
                message = BeeUtils.concat(1, viewName, "deleted", responseCount, "rows of",
                    requestCount, "requested");
                BeeKeeper.getLog().warning(message);
              }

              if (callback != null) {
                if (responseCount > 0) {
                  callback.onSuccess(responseCount);
                } else {
                  callback.onFailure(new String[] {message});
                }
              }

            } else {
              BeeKeeper.getLog().severe(viewName, "delete", requestCount, "rows");
              BeeKeeper.getLog().severe("response:", s);
              if (callback != null) {
                callback.onFailure(new String[] {s});
              }
            }
          }
        });
  }

  public static int getColumnValues(String tableName, String columnName,
      Filter filter, boolean ascending, RowSetCallback callback) {
    return getColumnValues(tableName, columnName, filter,
        new Order(columnName, ascending), callback);
  }
  
  public static int getColumnValues(String tableName, String columnName,
      Filter filter, Order order, RowSetCallback callback) {
    return getColumnValues(tableName, columnName, filter, order, BeeConst.UNDEF, BeeConst.UNDEF,
        callback);
  }

  public static int getColumnValues(String tableName, String columnName,
      final Filter filter, final Order order, final int offset, final int limit,
      final RowSetCallback callback) {
    Assert.notEmpty(tableName);
    Assert.notEmpty(columnName);
    Assert.notNull(callback);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, tableName,
        Service.VAR_VIEW_COLUMNS, columnName);

    if (filter != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, filter.serialize());
    }
    if (order != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_ORDER, order.serialize());
    }
    if (offset >= 0 && limit > 0) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_OFFSET, offset,
          Service.VAR_VIEW_LIMIT, limit);
    }

    return BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.QUERY,
        RpcParameter.SECTION.DATA, lst),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasResponse(BeeRowSet.class)) {
              BeeRowSet rs = BeeRowSet.restore((String) response.getResponse());
              callback.onSuccess(rs);
            }
          }
        });
  }

  public static void getRowCount(final String viewName, final Filter filter,
      final IntCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);
    if (filter != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, filter.serialize());
    }

    BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.COUNT_ROWS,
        RpcParameter.SECTION.DATA, lst),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);
            String s = (String) response.getResponse();

            if (BeeUtils.isDigit(s)) {
              int rowCount = BeeUtils.toInt(s);
              BeeKeeper.getLog().info(viewName, filter, "row count:", rowCount);
              callback.onSuccess(rowCount);
            } else {
              String message = BeeUtils.concat(1, viewName, filter, "row count response:", s);
              BeeKeeper.getLog().severe(message);
              callback.onFailure(new String[] {message});
            }
          }
        });
  }

  public static void getRowCount(String viewName, final IntCallback callback) {
    getRowCount(viewName, null, callback);
  }

  public static int getRowSet(String viewName, Filter filter, Order order,
      CachingPolicy cachingPolicy, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, -1, -1, cachingPolicy, callback);
  }

  public static int getRowSet(String viewName, Filter filter, Order order,
      int offset, int limit, CachingPolicy cachingPolicy, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, offset, limit, null, cachingPolicy, callback);
  }

  public static int getRowSet(String viewName, Filter filter, Order order,
      int offset, int limit, RowSetCallback callback) {
    return getRowSet(viewName, filter, order, offset, limit, CachingPolicy.NONE, callback);
  }

  public static int getRowSet(String viewName, final Filter filter, final Order order,
      final int offset, final int limit, String states, final CachingPolicy cachingPolicy,
      final RowSetCallback callback) {
    Assert.notEmpty(viewName);
    Assert.notNull(callback);

    if (cachingPolicy != null && cachingPolicy.doRead()) {
      BeeRowSet rowSet = Global.getCache().getRowSet(viewName, filter, order, offset, limit);
      if (rowSet != null) {
        callback.onSuccess(rowSet);
        return RESPONSE_FROM_CACHE;
      }
    }

    List<Property> lst = PropertyUtils.createProperties(Service.VAR_VIEW_NAME, viewName);

    if (filter != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_WHERE, filter.serialize());
    }
    if (order != null) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_ORDER, order.serialize());
    }

    if (offset >= 0 && limit > 0) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_OFFSET, offset,
          Service.VAR_VIEW_LIMIT, limit);
    }

    if (!BeeUtils.isEmpty(states)) {
      PropertyUtils.addProperties(lst, Service.VAR_VIEW_STATES, states);
    }

    return BeeKeeper.getRpc().makePostRequest(new ParameterList(Service.QUERY,
        RpcParameter.SECTION.DATA, lst),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasResponse(BeeRowSet.class)) {
              BeeRowSet rs = BeeRowSet.restore((String) response.getResponse());
              callback.onSuccess(rs);

              if (cachingPolicy != null && cachingPolicy.doWrite()) {
                Global.getCache().add(rs, filter, order, offset, limit);
              }
            }
          }
        });
  }

  public static int getRowSet(String viewName, Filter filter, Order order,
      RowSetCallback callback) {
    return getRowSet(viewName, filter, order, CachingPolicy.NONE, callback);
  }

  public static int getRowSet(String viewName, Filter filter, RowSetCallback callback) {
    return getRowSet(viewName, filter, null, callback);
  }

  public static int getRowSet(String viewName, Order order, RowSetCallback callback) {
    return getRowSet(viewName, null, order, callback);
  }

  public static int getRowSet(String viewName, RowSetCallback callback) {
    return getRowSet(viewName, null, null, callback);
  }

  public static boolean isResponseFromCache(int id) {
    return id == RESPONSE_FROM_CACHE;
  }

  public static void update(final BeeRowSet rs, boolean rowMode, final RowCallback callback) {
    Assert.notNull(rs);
    String service = rowMode ? Service.UPDATE_ROW : Service.UPDATE_CELL;

    BeeKeeper.getRpc().sendText(service, Codec.beeSerialize(rs),
        new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            Assert.notNull(response);

            if (response.hasErrors()) {
              if (callback != null) {
                callback.onFailure(response.getErrors());
              }

            } else {
              if (callback != null && response.hasResponse(BeeRow.class)) {
                callback.onSuccess(BeeRow.restore((String) response.getResponse()));
              }
            }
          }
        });
  }

  private Queries() {
  }
}
