package com.butent.bee.client.view;

import com.google.common.collect.Maps;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

public final class ViewFactory {

  public enum SupplierKind {
    GRID("grid_") {
      @Override
      void create(String item, ViewCallback callback) {
        GridFactory.openGrid(item, GridFactory.getGridInterceptor(item), null,
            getPresenterCallback(callback));
      }
    },

    FORM("form_") {
      @Override
      void create(final String item, final ViewCallback callback) {
        FormFactory.getFormDescription(item, new Callback<FormDescription>() {
          @Override
          public void onSuccess(FormDescription result) {
            FormFactory.openForm(result, FormFactory.getFormInterceptor(item),
                getPresenterCallback(callback));
          }
        });
      }
    },

    ROW_EDITOR("row_") {
      @Override
      void create(String item, ViewCallback callback) {
        RowEditor.parse(item, Opener.with(getPresenterCallback(callback)));
      }
    },

    CALENDAR("calendar_") {
      @Override
      void create(String item, ViewCallback callback) {
        Long id = BeeUtils.toLongOrNull(item);
        if (DataUtils.isId(id)) {
          CalendarKeeper.openCalendar(id, callback);
        }
      }
    };

    private static Pair<SupplierKind, String> parse(String key) {
      for (SupplierKind kind : SupplierKind.values()) {
        if (BeeUtils.isPrefix(key, kind.prefix)) {
          return Pair.of(kind, BeeUtils.removePrefix(key, kind.prefix));
        }
      }
      return null;
    }

    private final String prefix;

    private SupplierKind(String prefix) {
      this.prefix = prefix;
    }

    public String getKey(String item) {
      return prefix + BeeUtils.trim(item);
    }

    abstract void create(String item, ViewCallback callback);
  }

  private static final Map<String, ViewSupplier> suppliers = Maps.newHashMap();

  public static void clear() {
    suppliers.clear();
  }

  public static void create(String key, ViewCallback callback) {
    if (BeeUtils.isEmpty(key) || callback == null) {
      return;
    }

    ViewSupplier supplier = suppliers.get(BeeUtils.trim(key));
    if (supplier != null) {
      supplier.create(callback);

    } else {
      Pair<SupplierKind, String> pair = SupplierKind.parse(key);

      if (pair == null) {
        callback.onFailure("view supplier not found:", key);
      } else {
        pair.getA().create(pair.getB(), callback);
      }
    }
  }
  
  public static void createAndShow(String key) {
    Assert.notEmpty(key);

    create(key, new ViewCallback() {
      @Override
      public void onSuccess(View result) {
        BeeKeeper.getScreen().show(result);
      }
    });
  }

  public static Collection<String> getKeys() {
    return new TreeSet<String>(suppliers.keySet());
  }

  public static ViewSupplier getSupplier(String key) {
    if (BeeUtils.isEmpty(key)) {
      return null;
    } else {
      return suppliers.get(BeeUtils.trim(key));
    }
  }

  public static boolean hasSupplier(String key) {
    if (BeeUtils.isEmpty(key)) {
      return false;
    } else {
      return suppliers.containsKey(BeeUtils.trim(key));
    }
  }

  public static PresenterCallback getPresenterCallback(final ViewCallback callback) {
    return new PresenterCallback() {
      @Override
      public void onCreate(Presenter presenter) {
        callback.onSuccess(presenter.getMainView());
      }
    };
  }
  
  public static void registerSupplier(String key, ViewSupplier supplier) {
    if (!BeeUtils.isEmpty(key) && supplier != null && !hasSupplier(key)) {
      suppliers.put(BeeUtils.trim(key), supplier);
    }
  }

  private ViewFactory() {
  }
}
