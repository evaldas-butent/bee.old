package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.LinkedList;

@SuppressWarnings("serial")
public class RpcList extends LinkedList<RpcInfo> {
  private static final int DEFAULT_CAPACITY = 1000;

  public static String[] DEFAULT_INFO_COLUMNS = new String[]{
      RpcInfo.COL_ID, RpcInfo.COL_NAME, RpcInfo.COL_TYPE, RpcInfo.COL_STATE,
      RpcInfo.COL_START, RpcInfo.COL_TIMEOUT, RpcInfo.COL_EXPIRES,
      RpcInfo.COL_END, RpcInfo.COL_COMPLETED, RpcInfo.COL_REQ_MSG,
      RpcInfo.COL_REQ_ROWS, RpcInfo.COL_REQ_COLS, RpcInfo.COL_REQ_SIZE,
      RpcInfo.COL_REQ_INFO, RpcInfo.COL_RESP_MSG, RpcInfo.COL_RESP_ROWS,
      RpcInfo.COL_RESP_COLS, RpcInfo.COL_RESP_SIZE, RpcInfo.COL_RESP_INFO,
      RpcInfo.COL_ERR_MSG};

  private int capacity = DEFAULT_CAPACITY;

  public RpcList() {
    super();
  }

  public RpcList(int capacity) {
    this();
    this.capacity = capacity;
  }

  public void addInfo(RpcInfo el) {
    if (el != null) {
      checkCapacity();
      add(el);
    }
  }

  public int createInfo(String name, String msg) {
    RpcInfo el = new RpcInfo(name, msg);
    addInfo(el);

    return el.getId();
  }

  public int endError(int id, Exception ex) {
    RpcInfo el = locateInfo(id);

    if (el == null) {
      return BeeConst.TIME_UNKNOWN;
    } else {
      return el.endError(ex);
    }
  }

  public int endMessage(int id, String msg) {
    RpcInfo el = locateInfo(id);

    if (el == null) {
      return BeeConst.TIME_UNKNOWN;
    } else {
      return el.endMessage(msg);
    }
  }

  public int endResult(int id, int rows, int cols) {
    RpcInfo el = locateInfo(id);

    if (el == null) {
      return BeeConst.TIME_UNKNOWN;
    } else {
      return el.endResult(rows, cols);
    }
  }

  public int getCapacity() {
    return capacity;
  }

  public String[][] getDefaultInfo() {
    return getInfo(0, DEFAULT_INFO_COLUMNS);
  }

  public String[][] getDefaultInfo(int state) {
    return getInfo(state, DEFAULT_INFO_COLUMNS);
  }

  public String[][] getInfo(int state, String... cols) {
    int r = size();
    if (r <= 0) {
      return null;
    }

    int c = cols.length;
    Assert.parameterCount(c + 1, 2);

    boolean filterMd = state > 0;
    int i, j;
    String s;

    RpcList src;
    RpcInfo el;

    if (filterMd) {
      src = new RpcList();

      for (i = 0; i < r; i++) {
        el = get(i);
        if (el.filterState(state)) {
          src.add(el);
        }
      }

      if (src.isEmpty()) {
        return null;
      }
      r = src.size();
    } else {
      src = this;
    }

    String[][] arr = new String[r][c];

    for (i = 0; i < r; i++) {
      el = get(i);

      for (j = 0; j < c; j++) {
        if (BeeUtils.isEmpty(cols[j])) {
          arr[i][j] = BeeConst.STRING_EMPTY;
          continue;
        }

        if (BeeUtils.same(cols[j], RpcInfo.COL_ID)) {
          s = BeeUtils.transform(el.getId());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_NAME)) {
          s = el.getName();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_TYPE)) {
          s = el.getTypeString();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_STATE)) {
          s = el.getStateString();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_START)) {
          s = el.getStartTime();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_TIMEOUT)) {
          s = el.getTimeoutString();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_EXPIRES)) {
          s = el.getExpireTime();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_END)) {
          s = el.getEndTime();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_COMPLETED)) {
          s = el.getCompletedTime();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_REQ_MSG)) {
          s = el.getReqMsg();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_REQ_ROWS)) {
          s = el.getSizeString(el.getReqRows());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_REQ_COLS)) {
          s = el.getSizeString(el.getReqCols());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_REQ_SIZE)) {
          s = el.getSizeString(el.getReqSize());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_REQ_INFO)) {
          s = el.getReqInfoString();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_RESP_MSG)) {
          s = el.getRespMsg();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_RESP_ROWS)) {
          s = el.getSizeString(el.getRespRows());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_RESP_COLS)) {
          s = el.getSizeString(el.getRespCols());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_RESP_SIZE)) {
          s = el.getSizeString(el.getRespSize());
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_RESP_INFO)) {
          s = el.getRespInfoString();
        } else if (BeeUtils.same(cols[j], RpcInfo.COL_ERR_MSG)) {
          s = el.getErrMsg();
        } else {
          s = BeeConst.STRING_EMPTY;
        }

        if (BeeUtils.isEmpty(s)) {
          arr[i][j] = BeeConst.STRING_EMPTY;
        } else {
          arr[i][j] = s;
        }
      }
    }

    return arr;
  }

  public RpcInfo locateInfo(int id) {
    RpcInfo el = null;
    if (isEmpty()) {
      return el;
    }

    for (int i = 0; i < size(); i++) {
      if (get(i).getId() == id) {
        el = get(i);
        break;
      }
    }

    return el;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  public boolean updateRequestInfo(int id, int rows, int cols, int size) {
    RpcInfo el = locateInfo(id);

    if (el == null) {
      return false;
    } else {
      if (rows != BeeConst.SIZE_UNKNOWN) {
        el.setReqRows(rows);
      }
      if (cols != BeeConst.SIZE_UNKNOWN) {
        el.setReqCols(cols);
      }
      if (size != BeeConst.SIZE_UNKNOWN) {
        el.setReqSize(size);
      }
    }

    return true;
  }

  private void checkCapacity() {
    if (capacity > 0) {
      while (size() > capacity) {
        remove();
      }
    }
  }

}
