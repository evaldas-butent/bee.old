package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.modules.trade.TradeUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActTimeUnit;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PrintActForm extends AbstractFormInterceptor {

  Map<String, Widget> companies = new HashMap<>();
  List<Widget> totals = new ArrayList<>();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.inListSame(name, COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_SALE_PAYER,
        ClassifierConstants.COL_COMPANY)) {
      companies.put(name, widget.asWidget());

    } else if (BeeUtils.startsSame(name, "TotalInWords")) {
      totals.add(widget.asWidget());
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : companies.keySet()) {
      Long id = form.getLongValue(name);

      if (!DataUtils.isId(id) && !BeeUtils.same(name, COL_SALE_PAYER)) {
        id = BeeKeeper.getUser().getUserData().getCompany();
      }
      ClassifierUtils.getCompanyInfo(id, companies.get(name));
    }
    for (Widget total : totals) {
      TradeUtils.getTotalInWords(form.getDoubleValue(COL_TRADE_AMOUNT),
          form.getStringValue(AdministrationConstants.ALS_CURRENCY_NAME),
          form.getStringValue("MinorName"), total);
    }
    renderItems("TradeActItems");
    renderItems("TradeActServices");

    super.beforeRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintActForm();
  }

  private void renderItems(final String typeTable) {
    final Widget items = getFormView().getWidgetByName(typeTable);

    if (items == null) {
      return;
    }
    items.getElement().setInnerHTML(null);

    ParameterList args = TradeKeeper.createArgs(SVC_ITEMS_INFO);
    args.addDataItem("view_name", getViewName());
    args.addDataItem("id", getActiveRowId());
    args.addDataItem("TypeTable", typeTable);

    BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(BeeKeeper.getScreen());

        if (response.hasErrors()) {
          return;
        }
        SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

        if (rs.isEmpty()) {
          return;
        }
        Table<Long, String, String> data = TreeBasedTable.create();

        for (SimpleRowSet.SimpleRow row : rs) {
          Long id = row.getLong(typeTable);

          for (String col : rs.getColumnNames()) {
            String value = row.getValue(col);

            if (Objects.equals(col, COL_TA_RETURNED_QTY)) {
              BigDecimal remaining = row.getDecimal(COL_TRADE_ITEM_QUANTITY)
                  .subtract(BeeUtils.nvl(row.getDecimal(COL_TA_RETURNED_QTY), BigDecimal.ZERO));

              if (remaining.compareTo(BigDecimal.ZERO) != 0) {
                data.put(id, "RemainingQty", remaining.toPlainString());
              }
            }
            if (!BeeUtils.isEmpty(value)) {
              switch (col) {
                case COL_TA_SERVICE_FROM:
                case COL_TA_SERVICE_TO:
                  value = new JustDate(BeeUtils.toLong(value)).toString();
                  break;

                case COL_TRADE_TIME_UNIT:
                  value = EnumUtils.getCaption(TradeActTimeUnit.class, BeeUtils.toIntOrNull(value));
                  break;

                case COL_TA_SERVICE_MIN:
                  String daysPerWeek = row.getValue(COL_TA_SERVICE_DAYS);

                  data.put(id, COL_ITEM_NAME, BeeUtils.join("/", row.getValue(COL_ITEM_NAME),
                      row.getValue(COL_TRADE_ITEM_NOTE),
                      BeeUtils.joinWords("Minimalus nuomos terminas", value,
                          EnumUtils.getCaption(TradeActTimeUnit.class,
                              row.getInt(COL_TRADE_TIME_UNIT))),
                      BeeUtils.isEmpty(daysPerWeek) ? null : daysPerWeek + "d.per Sav."));
                  break;
              }
              data.put(id, col, value);
            }
          }
          double qty = BeeUtils.toDouble(data.get(id, COL_TRADE_ITEM_QUANTITY))
              - BeeUtils.toDouble(data.get(id, COL_TA_RETURNED_QTY));
          double sum = qty * BeeUtils.toDouble(data.get(id, COL_TRADE_ITEM_PRICE));
          double disc = BeeUtils.toDouble(data.get(id, COL_TRADE_DISCOUNT));
          double vat = BeeUtils.toDouble(data.get(id, COL_TRADE_VAT));
          boolean vatInPercents = BeeUtils.toBoolean(data.get(id, COL_TRADE_VAT_PERC));

          double dscSum = sum / 100 * disc;
          sum -= dscSum;

          if (BeeUtils.toBoolean(data.get(id, COL_TRADE_VAT_PLUS))) {
            if (vatInPercents) {
              vat = sum / 100 * vat;
            }
          } else {
            if (vatInPercents) {
              vat = sum - sum / (1 + vat / 100);
            }
            sum -= vat;
          }
          sum = BeeUtils.round(sum, 2);

          for (String col : new String[] {COL_ITEM_WEIGHT, COL_ITEM_AREA}) {
            if (data.contains(id, col)) {
              data.put(id, col,
                  BeeUtils.toString(BeeUtils.round(BeeUtils.toDouble(data.get(id, col)) * qty, 5)));
            }
          }
          if (disc > 0) {
            data.put(id, COL_TRADE_DISCOUNT,
                BeeUtils.removeTrailingZeros(BeeUtils.toString(disc)) + "%");
          }
          if (vat > 0) {
            data.put(id, COL_TRADE_VAT, BeeUtils.toString(BeeUtils.round(vat, 2)));
          }
          data.put(id, COL_TRADE_ITEM_PRICE, BeeUtils.removeTrailingZeros(BeeUtils
              .toString(BeeUtils.round((sum + dscSum) / (qty != 0 ? qty : 1d), 5))));
          data.put(id, "Amount", BeeUtils.toString(BeeUtils.round(sum, 2)));
          data.put(id, "AmountTotal", BeeUtils.toString(BeeUtils.round(sum + vat, 2)));
        }
        HtmlTable table = new HtmlTable(TradeUtils.STYLE_ITEMS_TABLE);
        int c = 0;

        Set<String> calc = new HashSet<>();
        calc.add(COL_TRADE_ITEM_QUANTITY);
        calc.add(COL_TA_RETURNED_QTY);
        calc.add("RemainingQty");
        calc.add(COL_TRADE_WEIGHT);
        calc.add(COL_ITEM_AREA);
        calc.add("Amount");
        calc.add(COL_TRADE_VAT);
        calc.add("AmountTotal");

        for (String col : new String[] {
            COL_ITEM_ARTICLE, COL_ITEM_NAME, COL_TA_SERVICE_FROM, COL_TA_SERVICE_TO,
            COL_TRADE_ITEM_QUANTITY, COL_UNIT, COL_TRADE_TIME_UNIT, COL_TA_RETURNED_QTY,
            "RemainingQty", COL_TRADE_WEIGHT, COL_ITEM_AREA, COL_TA_SERVICE_TARIFF,
            COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT, "Amount", COL_TRADE_VAT, "AmountTotal"}) {

          if (!data.containsColumn(col)) {
            continue;
          }
          table.setText(0, c, null, TradeUtils.STYLE_ITEMS + col);
          int r = 1;
          BigDecimal sum = BigDecimal.ZERO;

          for (Long id : data.rowKeySet()) {
            String value = data.get(id, col);

            if (calc.contains(col)) {
              sum = sum.add(BeeUtils.nvl(BeeUtils.toDecimalOrNull(value), BigDecimal.ZERO));
              value = BeeUtils.removeTrailingZeros(value);
            }
            table.setText(r++, c, value, TradeUtils.STYLE_ITEMS + col);
          }
          String value = null;

          if (sum.compareTo(BigDecimal.ZERO) != 0) {
            value = BeeUtils.removeTrailingZeros(sum.toPlainString());
          }
          table.setText(r, c, value, TradeUtils.STYLE_ITEMS + col);
          c++;
        }
        table.setText(table.getRowCount() - 1, 0, Localized.getConstants().totalOf());

        for (int i = 0; i < table.getRowCount(); i++) {
          table.getRowFormatter().addStyleName(i, i == 0 ? TradeUtils.STYLE_ITEMS_HEADER
              : (i < (table.getRowCount() - 1)
              ? TradeUtils.STYLE_ITEMS_DATA : TradeUtils.STYLE_ITEMS_FOOTER));
        }
        items.getElement().setInnerHTML(table.getElement().getString());
      }
    });
  }
}