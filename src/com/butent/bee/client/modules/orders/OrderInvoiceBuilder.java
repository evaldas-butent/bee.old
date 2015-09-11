package com.butent.bee.client.modules.orders;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.ALS_COMPANY_NAME;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.COL_COMPANY;
import static com.butent.bee.shared.modules.orders.OrdersConstants.VIEW_ORDERS_INVOICES;
import static com.butent.bee.shared.modules.orders.OrdersConstants.VIEW_ORDER_SALES;
import static com.butent.bee.shared.modules.trade.TradeConstants.ALS_CUSTOMER_NAME;
import static com.butent.bee.shared.modules.trade.TradeConstants.ALS_SUPPLIER_NAME;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_SALE;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.modules.trade.InvoiceBuilder;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.OrdersConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;

public class OrderInvoiceBuilder extends InvoiceBuilder {

  @Override
  public GridInterceptor getInstance() {
    return new OrderInvoiceBuilder();
  }

  @Override
  public String getRelationColumn() {
    return COL_SALE;
  }

  @Override
  protected ParameterList getRequestArgs() {
    return OrdersKeeper.createSvcArgs(OrdersConstants.SVC_CREATE_INVOICE_ITEMS);
  }

  @Override
  public String getTargetView() {
    return VIEW_ORDERS_INVOICES;
  }

  @Override
  protected void createInvoice(BeeRowSet data, BiConsumer<BeeRowSet, BeeRow> consumer) {

    DataInfo targetInfo = Data.getDataInfo(getTargetView());
    BeeRow newRow = RowFactory.createEmptyRow(targetInfo, true);

    if (data != null) {
      newRow.setValue(targetInfo.getColumnIndex(TradeConstants.COL_TRADE_CUSTOMER), data.getRow(0)
          .getLong(Data.getColumnIndex(VIEW_ORDER_SALES, COL_COMPANY)));
      newRow.setValue(targetInfo.getColumnIndex(ALS_CUSTOMER_NAME), data.getRow(0)
          .getString(Data.getColumnIndex(VIEW_ORDER_SALES, ALS_COMPANY_NAME)));
    }
    newRow.setValue(targetInfo.getColumnIndex(TradeConstants.COL_TRADE_SUPPLIER), BeeKeeper
        .getUser().getCompany());

    newRow
        .setValue(targetInfo.getColumnIndex(ALS_SUPPLIER_NAME), BeeKeeper.getUser()
            .getCompanyName());

    newRow.setValue(targetInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER), BeeKeeper
        .getUser().getUserId());

    newRow.setValue(targetInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER
        + ClassifierConstants.COL_FIRST_NAME), BeeKeeper.getUser().getFirstName());

    newRow.setValue(targetInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER
        + ClassifierConstants.COL_LAST_NAME), BeeKeeper.getUser().getLastName());
    consumer.accept(data, newRow);
  }
}