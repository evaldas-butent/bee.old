package com.butent.bee.client.presenter;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.data.ParentRowCreator;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.AutocompleteProvider;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormAndHeader;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.HandlesActions;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class NewRowPresenter extends AbstractPresenter implements ParentRowCreator,
    ReadyForInsertEvent.Handler, HasViewName {

  public static final String STYLE_CONTAINER = BeeConst.CSS_CLASS_PREFIX + "NewRowContainer";
  public static final String STYLE_HEADER = BeeConst.CSS_CLASS_PREFIX + "NewRowHeader";
  public static final String STYLE_CAPTION = BeeConst.CSS_CLASS_PREFIX + "NewRowCaption";

  private static final EnumSet<UiOption> uiOptions = EnumSet.of(UiOption.EDITOR);

  private static HeaderView createHeader(String caption, Set<Action> enabledActions) {
    HeaderView formHeader = new HeaderImpl();
    formHeader.asWidget().addStyleName(STYLE_HEADER);

    Set<Action> actions = EnumSet.of(Action.SAVE, Action.CLOSE);
    if (!BeeUtils.isEmpty(enabledActions)) {
      actions.addAll(enabledActions);
    }

    formHeader.create(caption, false, false, null, uiOptions,
        actions, Action.NO_ACTIONS, Action.NO_ACTIONS);
    formHeader.addCaptionStyle(STYLE_CAPTION);

    return formHeader;
  }

  private final DataInfo dataInfo;

  private final FormAndHeader container;
  private final FormView formView;

  private HandlesActions actionDelegate;

  public NewRowPresenter(FormView formView, DataInfo dataInfo, String caption,
      Set<Action> enabledActions) {

    this.formView = formView;
    this.dataInfo = dataInfo;

    HeaderView header = createHeader(caption, enabledActions);
    this.container = createContainer(header);

    container.setViewPresenter(this);

    formView.addReadyForInsertHandler(this);
  }

  @Override
  public void createParentRow(NotificationListener notificationListener,
      final Callback<IsRow> callback) {

    if (!formView.validate(notificationListener, false)) {
      return;
    }

    GridView gridView = formView.getBackingGrid();
    if (gridView != null && !gridView.validateFormData(formView, notificationListener, false)) {
      return;
    }

    IsRow row = formView.getActiveRow();

    if (DataUtils.isNewRow(row)) {
      insert(row, true, new RowCallback() {
        @Override
        public void onFailure(String... reason) {
          if (callback != null) {
            callback.onFailure(reason);
          }
        }

        @Override
        public void onSuccess(BeeRow result) {
          formView.observeData();
          formView.updateRow(result, true);
          if (callback != null) {
            callback.onSuccess(result);
          }
        }
      });
    } else if (callback != null) {
      callback.onSuccess(row);
    }
  }

  @Override
  public String getCaption() {
    for (Widget child : container) {
      if (child instanceof HasCaption) {
        return ((HasCaption) child).getCaption();
      }
    }
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return ViewHelper.getHeader(getMainView().asWidget());
  }

  @Override
  public View getMainView() {
    return container;
  }

  @Override
  public String getViewKey() {
    return formView.getSupplierKey();
  }

  @Override
  public String getViewName() {
    return dataInfo.getViewName();
  }

  @Override
  public void handleAction(Action action) {
    if (getActionDelegate() != null) {
      getActionDelegate().handleAction(action);
    }
  }

  @Override
  public void onReadyForInsert(final ReadyForInsertEvent event) {
    Queries.insert(getViewName(), event.getColumns(), event.getValues(), event.getChildren(),
        new RowCallback() {
          @Override
          public void onFailure(String... reason) {
            if (event.getCallback() == null) {
              formView.notifySevere(reason);
            } else {
              event.getCallback().onFailure(reason);
            }
          }

          @Override
          public void onSuccess(BeeRow result) {
            RowInsertEvent.fire(BeeKeeper.getBus(), getViewName(), result, event.getSourceId());

            if (formView.getFormInterceptor() != null) {
              formView.getFormInterceptor().afterInsertRow(result, event.isForced());
            }

            if (event.getCallback() != null) {
              event.getCallback().onSuccess(result);
            }
          }
        });
  }

  public void save(final RowCallback callback) {
    if (!formView.validate(formView, true)) {
      return;
    }

    final GridView gridView = formView.getBackingGrid();
    if (gridView != null) {
      if (!gridView.validateFormData(formView, formView, true)) {
        return;
      }

      if (gridView.likeAMotherlessChild()) {
        gridView.ensureRelId(result -> {
          if (!gridView.likeAMotherlessChild()) {
            save(callback);
          }
        });

        return;
      }
    }

    IsRow row = formView.getActiveRow();

    if (DataUtils.isNewRow(row)) {
      insert(row, false, new RowCallback() {
        @Override
        public void onFailure(String... reason) {
          formView.notifySevere(reason);
        }

        @Override
        public void onSuccess(BeeRow result) {
          if (callback != null) {
            callback.onSuccess(result);
          }
        }
      });

    } else {
      int upd = Queries.update(getViewName(), dataInfo.getColumns(), formView.getOldRow(),
          row, formView.getChildrenForUpdate(), new RowCallback() {
            @Override
            public void onFailure(String... reason) {
              formView.notifySevere(reason);
            }

            @Override
            public void onSuccess(BeeRow result) {
              RowUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), result);

              if (formView.getFormInterceptor() != null) {
                formView.getFormInterceptor().afterUpdateRow(result);
              }

              if (gridView != null && gridView.getGridInterceptor() != null) {
                gridView.getGridInterceptor().afterUpdateRow(result);
              }

              if (callback != null) {
                callback.onSuccess(result);
              }
            }
          });

      if (upd == 0 && callback != null && row instanceof BeeRow) {
        callback.onSuccess((BeeRow) row);
      }
    }
  }

  public void setActionDelegate(HandlesActions actionDelegate) {
    this.actionDelegate = actionDelegate;
  }

  private FormAndHeader createContainer(HeaderView headerView) {
    FormAndHeader formContainer = new FormAndHeader();
    formContainer.addStyleName(STYLE_CONTAINER);
    formContainer.addStyleName(UiOption.getStyleName(uiOptions));

    StyleUtils.updateAppearance(formContainer,
        formView.getContainerClassName(), formView.getContainerStyle());

    formContainer.addTopHeightFillHorizontal(headerView.asWidget(), 0, headerView.getHeight());
    formContainer.addTopBottomFillHorizontal(formView.asWidget(), headerView.getHeight(), 0);

    return formContainer;
  }

  private HandlesActions getActionDelegate() {
    return actionDelegate;
  }

  private void insert(IsRow row, boolean forced, RowCallback callback) {
    List<BeeColumn> columns = new ArrayList<>();
    List<String> values = new ArrayList<>();

    GridView gridView = formView.getBackingGrid();
    String relColumn = (gridView == null) ? null : gridView.getRelColumn();

    for (int i = 0; i < dataInfo.getColumnCount(); i++) {
      BeeColumn column = dataInfo.getColumns().get(i);
      String value = row.getString(i);

      if (!BeeUtils.isEmpty(relColumn) && BeeUtils.same(relColumn, column.getId())) {
        Long relId = gridView.getRelId();

        if (DataUtils.isId(relId)) {
          columns.add(column);
          values.add(BeeUtils.toString(relId));

        } else {
          callback.onFailure(BeeUtils.joinWords(getViewName(), relColumn,
              Localized.dictionary().invalidIdValue(relId)));
          return;
        }

      } else if (column.isInsertable(value)) {
        columns.add(column);
        values.add(value);
      }
    }

    if (columns.isEmpty()) {
      callback.onFailure(getViewName(), Localized.dictionary().newRow(),
          Localized.dictionary().allValuesCannotBeEmpty());
      return;
    }

    AutocompleteProvider.retainValues(formView);

    ReadyForInsertEvent event = new ReadyForInsertEvent(columns, values,
        formView.getChildrenForInsert(), callback, formView.getId());
    event.setForced(forced);

    if (formView.getFormInterceptor() != null) {
      formView.getFormInterceptor().onReadyForInsert(formView, event);
      if (event.isConsumed()) {
        return;
      }
    }

    if (gridView != null && gridView.getGridInterceptor() != null) {
      gridView.getGridInterceptor().onReadyForInsert(gridView, event);
      if (event.isConsumed()) {
        return;
      }
    }

    formView.fireEvent(event);
  }
}
