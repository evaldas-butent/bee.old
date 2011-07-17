package com.butent.bee.client.data;

import com.google.common.collect.Lists;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.event.SortEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

/**
 * Enables to manage ranges of data shown in user interface tables.
 */

public abstract class Provider implements SortEvent.Handler, HandlesAllDataEvents, HasViewName {

  private final HasDataTable display;
  private final String viewName;

  private final List<HandlerRegistration> handlerRegistry = Lists.newArrayList();

  private boolean rangeChangeEnabled = true;
  private boolean cacheEnabled = true;

  private Filter filter = null;
  private Order order = null;

  protected Provider(HasDataTable display, String viewName) {
    Assert.notNull(display);
    this.display = display;
    this.viewName = viewName;

    this.handlerRegistry.add(display.addRangeChangeHandler(new RangeChangeEvent.Handler() {
      public void onRangeChange(RangeChangeEvent event) {
        if (rangeChangeEnabled) {
          onRangeChanged(false);
        }
      }
    }));

    this.handlerRegistry.add(display.addSortHandler(this));
    this.handlerRegistry.addAll(BeeKeeper.getBus().registerDataHandler(this));
  }

  public void disableCache() {
    setCacheEnabled(false);
  }

  public void disableRangeChange() {
    setRangeChangeEnabled(false);
  }

  public void enableCache() {
    setCacheEnabled(true);
  }

  public void enableRangeChange() {
    setRangeChangeEnabled(true);
  }

  public Filter getFilter() {
    return filter;
  }

  public Order getOrder() {
    return order;
  }

  public String getViewName() {
    return viewName;
  }

  public boolean isCacheEnabled() {
    return cacheEnabled;
  }

  public boolean isRangeChangeEnabled() {
    return rangeChangeEnabled;
  }

  public void onCellUpdate(CellUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onCellUpdate(event);
    }
  }

  public void onFilterChanged(Filter newFilter, int rowCount) {
    setFilter(newFilter);
    getDisplay().setRowCount(rowCount);
    goTop();
  }

  public void onMultiDelete(MultiDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      disableCache();
      getDisplay().onMultiDelete(event);
      onRangeChanged(false);
      enableCache();
    }
  }

  public void onRowDelete(RowDeleteEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      disableCache();
      getDisplay().onRowDelete(event);
      onRangeChanged(false);
      enableCache();
    }
  }

  public abstract void onRowInsert(RowInsertEvent event);
  
  public void onRowUpdate(RowUpdateEvent event) {
    if (BeeUtils.same(getViewName(), event.getViewName())) {
      getDisplay().onRowUpdate(event);
    }
  }

  public abstract void onSort(SortEvent event);

  public void onUnload() {
    for (HandlerRegistration entry : handlerRegistry) {
      if (entry != null) {
        entry.removeHandler();
      }
    }
  }

  public void refresh() {
    startLoading();
    Global.getCache().removeQuietly(getViewName());

    final Filter flt = getFilter();
    Queries.getRowCount(getViewName(), flt, new Queries.IntCallback() {
      public void onFailure(String[] reason) {
      }

      public void onSuccess(Integer result) {
        if (result <= 0) {
          BeeKeeper.getLog().warning(getViewName(), flt, "refresh: row count", result);
        }
        getDisplay().setRowCount(result);
        onRefresh();
      }
    });
  }

  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void setRangeChangeEnabled(boolean rangeChangeEnabled) {
    this.rangeChangeEnabled = rangeChangeEnabled;
  }

  protected HasDataTable getDisplay() {
    return display;
  }

  protected int getPageSize() {
    return getDisplay().getVisibleRange().getLength();
  }

  protected Range getRange() {
    return getDisplay().getVisibleRange();
  }

  protected void goTop() {
    getDisplay().setPageStart(0);
    onRangeChanged(true);
  }

  protected abstract void onRangeChanged(boolean updateActiveRow);

  protected abstract void onRefresh();

  protected void startLoading() {
    getDisplay().fireLoadingStateChange(LoadingStateChangeEvent.LoadingState.LOADING);
  }
}
