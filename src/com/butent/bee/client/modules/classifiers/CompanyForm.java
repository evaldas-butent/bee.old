package com.butent.bee.client.modules.classifiers;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ModalGrid;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.values.Display;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Map;

public class CompanyForm extends AbstractFormInterceptor {

  private static final String WIDGET_FINANCIAL_STATE_AUDIT_NAME = COL_COMPANY_FINANCIAL_STATE
      + "Audit";

  @SuppressWarnings("unused")
  private InputBoolean wRemindEmail;
  @SuppressWarnings("unused")
  private DataSelector wEmail;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid && BeeUtils.same(name, GRID_COMPANY_BANK_ACCOUNTS)) {
      ChildGrid grid = (ChildGrid) widget;

      grid.setGridInterceptor(new AbstractGridInterceptor() {

        @Override
        public void afterCreatePresenter(GridPresenter presenter) {
          HeaderView header = presenter.getHeader();
          header.clearCommandPanel();

          FaLabel setDefault = new FaLabel(FontAwesome.CHECK);
          setDefault.setTitle(Localized.getConstants().setAsPrimary());
          setDefault.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
              GridView gridView = getGridPresenter().getGridView();

              IsRow selectedRow = gridView.getActiveRow();
              if (selectedRow == null) {
                gridView.notifyWarning(Localized.getConstants().selectAtLeastOneRow());
                return;
              } else {
                setAsPrimaryAccount(selectedRow.getId());
              }
            }
          });

          header.addCommandItem(setDefault);
        }

        @Override
        public void afterInsertRow(IsRow accountsRow) {
          setAsPrimaryAccount(accountsRow.getId(), true);
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }

        private void setAsPrimaryAccount(Long companyBankAccount) {
          setAsPrimaryAccount(companyBankAccount, false);
        }

        private void setAsPrimaryAccount(final Long companyBankAccount, boolean checkDefault) {
          final IsRow companyRow = getFormView().getActiveRow();
          final IsRow companyRowOld = getFormView().getOldRow();
          final int defBankAccFieldId = Data.getColumnIndex(VIEW_COMPANIES,
              COL_DEFAULT_BANK_ACCOUNT);

          boolean hasDefault =
              DataUtils.isId(companyRow.getLong(defBankAccFieldId));

          boolean canChange = !hasDefault || !checkDefault;

          if (canChange) {
            Queries.update(getFormView().getViewName(), Filter.compareId(companyRow.getId()),
                COL_DEFAULT_BANK_ACCOUNT, Value.getValue(companyBankAccount), new IntCallback() {

                  @Override
                  public void onSuccess(Integer result) {
                    companyRow.setValue(defBankAccFieldId, companyBankAccount);
                    companyRowOld.setValue(defBankAccFieldId, companyBankAccount);
                    DataChangeEvent.fireRefresh(BeeKeeper.getBus(), getGridView().getViewName());
                  }
                });
          }
        }

      });
    }

    if (widget instanceof HasClickHandlers
        && BeeUtils.same(name, WIDGET_FINANCIAL_STATE_AUDIT_NAME)) {

      HasClickHandlers button = (HasClickHandlers) widget;
      button.addClickHandler(getFinancialStateAuditClickHandler());

      if (widget instanceof UIObject) {
        ((UIObject) widget).setTitle(Localized.getConstants().actionAudit());
      }
    }

    if (widget instanceof InputBoolean && BeeUtils.same(name, COL_REMIND_EMAIL)) {
      wRemindEmail = (InputBoolean) widget;
      StyleUtils.setDisplay(widget.getElement(), Display.INLINE_BLOCK);

      if (widget instanceof UIObject) {
        ((UIObject) widget).setTitle(Localized.getConstants().sendReminder());
      }
    }

    if (widget instanceof DataSelector && BeeUtils.same(name, COL_EMAIL_ID)) {
      wEmail = (DataSelector) widget;
    }
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    refreshCreditInfo();
  }

  @Override
  public FormInterceptor getInstance() {
    return new CompanyForm();
  }

  private ClickHandler getFinancialStateAuditClickHandler() {
    return new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        FormView formView = getFormView();

        if (formView == null) {
          return;
        }

        IsRow activeRow = formView.getActiveRow();

        if (activeRow == null) {
          return;
        }

        GridFactory.openGrid(AdministrationConstants.GRID_HISTORY,
            new FinancialStateHistoryHandler(formView.getViewName(),
                Lists.newArrayList(Long.valueOf(activeRow.getId()))),
            null, ModalGrid.opener(500, 500));
      }
    };
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {

    if (BeeUtils.same(name, WIDGET_FINANCIAL_STATE_AUDIT_NAME)) {
      return BeeKeeper.getUser().isWidgetVisible(RegulatedWidget.AUDIT);
    }

    return super.beforeCreateWidget(name, description);
  }

  @Override
  public boolean onStartEdit(FormView form, IsRow row, ScheduledCommand focusCommand) {
    // TODO:
    // if (wRemindEmail != null && wEmail != null) {
    // if (BeeUtils.isEmpty(wEmail.getValue())) {
    // wRemindEmail.setEnabled(false);
    // } else {
    // wRemindEmail.setEnabled(true);
    // }
    // }

    return super.onStartEdit(form, row, focusCommand);
  }

  private void refreshCreditInfo() {
    final FormView form = getFormView();
    final Widget widget = form.getWidgetByName(SVC_CREDIT_INFO);

    if (widget != null) {
      widget.getElement().setInnerText(null);

      ParameterList args = TradeKeeper.createArgs(SVC_CREDIT_INFO);
      args.addDataItem(COL_COMPANY, getActiveRow().getId());

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          response.notify(form);

          if (response.hasErrors()) {
            return;
          }
          Map<String, String> result = Codec.deserializeMap(response.getResponseAsString());

          if (!BeeUtils.isEmpty(result)) {
            HtmlTable table = new HtmlTable();
            table.setColumnCellStyles(1, "text-align:right; font-weight:bold;color:red;");
            int c = 0;

            String amount = result.get(VAR_DEBT);

            if (BeeUtils.isPositiveDouble(amount)) {
              table.setHtml(c, 0, Localized.getConstants().trdDebt());
              table.setHtml(c, 1, amount);
              double limit = BeeUtils.toDouble(result.get(COL_COMPANY_CREDIT_LIMIT));

              if (BeeUtils.toDouble(amount) <= limit) {
                StyleUtils.setColor(table.getCellFormatter().getElement(c, 1), "black");
              }
              c++;
            }
            amount = result.get(VAR_OVERDUE);

            if (BeeUtils.isPositiveDouble(amount)) {
              table.setHtml(c, 0, Localized.getConstants().trdOverdue());
              table.setHtml(c, 1, amount);
            }
            widget.getElement().setInnerHTML(table.getElement().getString());
          }
        }
      });
    }
  }
}
