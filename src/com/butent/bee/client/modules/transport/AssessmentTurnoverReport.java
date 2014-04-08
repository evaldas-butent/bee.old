package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.ReportInterceptor;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public class AssessmentTurnoverReport extends ReportInterceptor {

  private static BeeLogger logger = LogUtils.getLogger(AssessmentTurnoverReport.class);

  private static final String NAME_START_DATE = "StartDate";
  private static final String NAME_END_DATE = "EndDate";

  private static final String NAME_CURRENCY = "Currency";

  private static final String NAME_DEPARTMENTS = "Departments";
  private static final String NAME_MANAGERS = "Managers";
  private static final String NAME_CUSTOMERS = "Customers";

  private static final List<String> NAME_GROUP_BY =
      Lists.newArrayList("Group0", "Group1", "Group2", "Group3");

  private static final String STYLE_PREFIX = StyleUtils.CLASS_NAME_PREFIX + "tr-atr-";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_HEADER = STYLE_PREFIX + "header";
  private static final String STYLE_HEADER_1 = STYLE_HEADER + "-1";
  private static final String STYLE_HEADER_2 = STYLE_HEADER + "-2";

  private static final String STYLE_YEAR = STYLE_PREFIX + "year";
  private static final String STYLE_MONTH = STYLE_PREFIX + "month";
  private static final String STYLE_DEPARTMENT = STYLE_PREFIX + "department";
  private static final String STYLE_MANAGER = STYLE_PREFIX + "manager";
  private static final String STYLE_CUSTOMER = STYLE_PREFIX + "customer";

  private static final String STYLE_QUANTITY = STYLE_PREFIX + "quantity";
  private static final String STYLE_INCOME = STYLE_PREFIX + "income";
  private static final String STYLE_EXPENSE = STYLE_PREFIX + "expense";
  private static final String STYLE_PROFIT = STYLE_PREFIX + "profit";
  private static final String STYLE_MARGIN = STYLE_PREFIX + "margin";

  private static final String STYLE_AMOUNT = STYLE_PREFIX + "amount";
  // private static final String STYLE_GROWTH = STYLE_PREFIX + "growth";

  private static final String STYLE_SECONDARY = STYLE_PREFIX + "secondary";

  private static final String STYLE_DETAILS = STYLE_PREFIX + "details";
  private static final String STYLE_SUMMARY = STYLE_PREFIX + "summary";

  private static final String DRILL_DOWN_GRID_NAME = "AssessmentReportDrillDown";

  private static double margin(Double profit, Double income) {
    if (BeeUtils.isDouble(profit) && BeeUtils.isDouble(income) && !BeeUtils.isZero(income)) {
      return profit * 100d / income;
    } else {
      return BeeConst.DOUBLE_ZERO;
    }
  }

  private static double profit(Double income, Double expense) {
    return BeeUtils.unbox(income) - BeeUtils.unbox(expense);
  }

  AssessmentTurnoverReport() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentTurnoverReport();
  }

  @Override
  public void onLoad(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    Widget widget = form.getWidgetByName(NAME_START_DATE);
    DateTime dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_START_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_END_DATE);
    dateTime = BeeKeeper.getStorage().getDateTime(storageKey(NAME_END_DATE, user));
    if (widget instanceof InputDateTime && dateTime != null) {
      ((InputDateTime) widget).setDateTime(dateTime);
    }

    widget = form.getWidgetByName(NAME_CURRENCY);
    Long currency = BeeKeeper.getStorage().getLong(storageKey(NAME_CURRENCY, user));
    if (widget instanceof UnboundSelector && DataUtils.isId(currency)) {
      ((UnboundSelector) widget).setValue(currency, false);
    }

    List<String> selectorNames = Lists.newArrayList(NAME_DEPARTMENTS, NAME_MANAGERS,
        NAME_CUSTOMERS);

    for (String selectorName : selectorNames) {
      widget = form.getWidgetByName(selectorName);
      String idList = BeeKeeper.getStorage().get(storageKey(selectorName, user));
      if (widget instanceof MultiSelector && !BeeUtils.isEmpty(idList)) {
        ((MultiSelector) widget).render(idList);
      }
    }
    for (String groupName : NAME_GROUP_BY) {
      widget = form.getWidgetByName(groupName);
      Integer index = BeeKeeper.getStorage().getInteger(storageKey(groupName, user));
      if (widget instanceof ListBox && BeeUtils.isPositive(index)) {
        ((ListBox) widget).setSelectedIndex(index);
      }
    }
  }

  @Override
  public void onUnload(FormView form) {
    Long user = BeeKeeper.getUser().getUserId();
    if (!DataUtils.isId(user)) {
      return;
    }

    BeeKeeper.getStorage().set(storageKey(NAME_START_DATE, user), getDateTime(NAME_START_DATE));
    BeeKeeper.getStorage().set(storageKey(NAME_END_DATE, user), getDateTime(NAME_END_DATE));

    BeeKeeper.getStorage().set(storageKey(NAME_CURRENCY, user), getEditorValue(NAME_CURRENCY));

    BeeKeeper.getStorage().set(storageKey(NAME_DEPARTMENTS, user),
        getEditorValue(NAME_DEPARTMENTS));
    BeeKeeper.getStorage().set(storageKey(NAME_MANAGERS, user),
        getEditorValue(NAME_MANAGERS));
    BeeKeeper.getStorage().set(storageKey(NAME_CUSTOMERS, user),
        getEditorValue(NAME_CUSTOMERS));

    for (String groupName : NAME_GROUP_BY) {
      Widget widget = form.getWidgetByName(groupName);
      if (widget instanceof ListBox) {
        Integer index = ((ListBox) widget).getSelectedIndex();
        if (!BeeUtils.isPositive(index)) {
          index = null;
        }

        BeeKeeper.getStorage().set(storageKey(groupName, user), index);
      }
    }
  }

  @Override
  protected void clearFilter() {
    clearEditor(NAME_START_DATE);
    clearEditor(NAME_END_DATE);

    clearEditor(NAME_DEPARTMENTS);
    clearEditor(NAME_MANAGERS);
    clearEditor(NAME_CUSTOMERS);
  }

  @Override
  protected void doReport() {
    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (!checkRange(start, end)) {
      return;
    }

    ParameterList params = TransportHandler.createArgs(SVC_GET_ASSESSMENT_TURNOVER_REPORT);

    if (start != null) {
      params.addDataItem(Service.VAR_FROM, start.getTime());
    }
    if (end != null) {
      params.addDataItem(Service.VAR_TO, end.getTime());
    }

    Long currency = BeeUtils.toLongOrNull(getEditorValue(NAME_CURRENCY));
    if (DataUtils.isId(currency)) {
      params.addDataItem(AdministrationConstants.COL_CURRENCY, currency);
    }

    String departments = getEditorValue(NAME_DEPARTMENTS);
    if (!BeeUtils.isEmpty(departments)) {
      params.addDataItem(AR_DEPARTMENT, departments);
    }
    String managers = getEditorValue(NAME_MANAGERS);
    if (!BeeUtils.isEmpty(managers)) {
      params.addDataItem(AR_MANAGER, managers);
    }
    String customers = getEditorValue(NAME_CUSTOMERS);
    if (!BeeUtils.isEmpty(customers)) {
      params.addDataItem(AR_CUSTOMER, customers);
    }

    List<String> groupBy = Lists.newArrayList();
    for (String groupName : NAME_GROUP_BY) {
      Widget widget = getFormView().getWidgetByName(groupName);

      if (widget instanceof ListBox) {
        int index = ((ListBox) widget).getSelectedIndex();
        String group;

        switch (index) {
          case 1:
            group = BeeConst.MONTH;
            break;
          case 2:
            group = AR_DEPARTMENT;
            break;
          case 3:
            group = AR_MANAGER;
            break;
          case 4:
            group = AR_CUSTOMER;
            break;
          default:
            group = null;
        }

        if (group != null && !groupBy.contains(group)) {
          groupBy.add(group);
        }
      }
    }

    if (!groupBy.isEmpty()) {
      params.addDataItem(Service.VAR_GROUP_BY, NameUtils.join(groupBy));
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasMessages()) {
          response.notify(getFormView());
        }

        if (response.hasResponse(SimpleRowSet.class)) {
          renderData(SimpleRowSet.restore(response.getResponseAsString()));
        } else {
          getFormView().notifyWarning(Localized.getConstants().nothingFound());
        }
      }
    });
  }

  @Override
  protected String getStorageKeyPrefix() {
    return "AssessmentTurnoverReport_";
  }

  private void renderData(final SimpleRowSet data) {
    HasIndexedWidgets container = getDataContainer();
    if (container == null) {
      return;
    }

    if (!container.isEmpty()) {
      container.clear();
    }

    final HtmlTable table = new HtmlTable(STYLE_TABLE);

    int row = 0;
    int c1 = 0;
    int c2 = 0;
    int col = 0;

    int colYear = BeeConst.UNDEF;
    int colMonth = BeeConst.UNDEF;
    int colDepartment = BeeConst.UNDEF;
    int colManager = BeeConst.UNDEF;
    int colCustomer = BeeConst.UNDEF;

    int colQuantity = BeeConst.UNDEF;
    int colIncome1 = BeeConst.UNDEF;
    int colExpense1 = BeeConst.UNDEF;
    int colProfit1 = BeeConst.UNDEF;
    int colMargin1 = BeeConst.UNDEF;

    int colSecondary = BeeConst.UNDEF;
    int colIncome2 = BeeConst.UNDEF;
    int colExpense2 = BeeConst.UNDEF;
    int colProfit2 = BeeConst.UNDEF;
    int colMargin2 = BeeConst.UNDEF;

    for (int j = 0; j < data.getNumberOfColumns(); j++) {
      String colName = data.getColumnName(j);

      switch (colName) {
        case BeeConst.YEAR:
          colYear = col;

          table.setText(row, c1, Localized.getConstants().year(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case BeeConst.MONTH:
          colMonth = col;

          table.setText(row, c1, Localized.getConstants().month(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AdministrationConstants.COL_DEPARTMENT:
          colDepartment = col;

          table.setText(row, c1, Localized.getConstants().department(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case AdministrationConstants.COL_DEPARTMENT_NAME:
          break;

        case ClassifierConstants.COL_COMPANY_PERSON:
          colManager = col;

          table.setText(row, c1, Localized.getConstants().manager(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case ClassifierConstants.COL_FIRST_NAME:
        case ClassifierConstants.COL_LAST_NAME:
          break;

        case COL_CUSTOMER:
          colCustomer = col;

          table.setText(row, c1, Localized.getConstants().customer(), STYLE_HEADER);
          table.getCellFormatter().setRowSpan(row, c1, 2);

          c1++;
          col++;
          break;

        case ClassifierConstants.ALS_COMPANY_NAME:
          break;

        case AR_RECEIVED:
        case AR_SECONDARY:
          String partLabel;
          if (colName.equals(AR_RECEIVED)) {
            colQuantity = col;
            partLabel = Localized.getConstants().trAssessmentReportAllOrders();
          } else {
            colSecondary = col;
            partLabel = Localized.getConstants().trAssessmentReportSecondary();
          }

          table.setText(row, c1, partLabel, STYLE_HEADER_1);
          table.getCellFormatter().setColSpan(row, c1, 9);

          table.setText(row + 1, c2, Localized.getConstants().quantity(), STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportGrowth(),
              STYLE_HEADER_2);

          c1++;
          c2 += 2;
          col += 2;
          break;

        case AR_INCOME:
        case AR_SECONDARY_INCOME:
          if (colName.equals(AR_INCOME)) {
            colIncome1 = col;
          } else {
            colIncome2 = col;
          }

          table.setText(row + 1, c2, Localized.getConstants().income(), STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportGrowth(),
              STYLE_HEADER_2);

          c2 += 2;
          col += 2;
          break;

        case AR_EXPENSE:
        case AR_SECONDARY_EXPENSE:
          if (colName.equals(AR_EXPENSE)) {
            colExpense1 = col;
            colProfit1 = col + 2;
            colMargin1 = col + 4;
          } else {
            colExpense2 = col;
            colProfit2 = col + 2;
            colMargin2 = col + 4;
          }

          table.setText(row + 1, c2, Localized.getConstants().trExpenses(), STYLE_HEADER_2);
          table.setText(row + 1, c2 + 1, Localized.getConstants().trAssessmentReportGrowth(),
              STYLE_HEADER_2);

          table.setText(row + 1, c2 + 2, Localized.getConstants().profit(), STYLE_HEADER_2);
          table.setText(row + 1, c2 + 3, Localized.getConstants().trAssessmentReportGrowth(),
              STYLE_HEADER_2);

          table.setText(row + 1, c2 + 4, Localized.getConstants().margin(), STYLE_HEADER_2);

          c2 += 5;
          col += 5;
          break;

        default:
          logger.warning("column not recognized", colName);
      }
    }

    int totQuantity = 0;
    double totIncome1 = BeeConst.DOUBLE_ZERO;
    double totExpense1 = BeeConst.DOUBLE_ZERO;

    int totSecondary = 0;
    double totIncome2 = BeeConst.DOUBLE_ZERO;
    double totExpense2 = BeeConst.DOUBLE_ZERO;

    row = 2;

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      int quantity = BeeUtils.unbox(data.getInt(i, AR_RECEIVED));
      Double income1 = data.getDouble(i, AR_INCOME);
      Double expense1 = data.getDouble(i, AR_EXPENSE);

      double profit1 = profit(income1, expense1);
      double margin1 = margin(profit1, income1);

      int secondary = BeeUtils.unbox(data.getInt(i, AR_SECONDARY));
      Double income2 = data.getDouble(i, AR_SECONDARY_INCOME);
      Double expense2 = data.getDouble(i, AR_SECONDARY_EXPENSE);

      double profit2 = profit(income2, expense2);
      double margin2 = margin(profit2, income2);

      for (int j = 0; j < data.getNumberOfColumns(); j++) {
        String colName = data.getColumnName(j);

        switch (colName) {
          case BeeConst.YEAR:
            table.setText(row, colYear, data.getValue(i, colName), STYLE_YEAR);
            break;

          case BeeConst.MONTH:
            table.setText(row, colMonth,
                Format.renderMonthFullStandalone(data.getInt(i, colName)), STYLE_MONTH);
            break;

          case AdministrationConstants.COL_DEPARTMENT_NAME:
            table.setText(row, colDepartment, data.getValue(i, colName), STYLE_DEPARTMENT);
            break;

          case ClassifierConstants.COL_FIRST_NAME:
            table.setText(row, colManager, BeeUtils.joinWords(data.getValue(i, colName),
                data.getValue(i, ClassifierConstants.COL_LAST_NAME)), STYLE_MANAGER);
            break;

          case ClassifierConstants.ALS_COMPANY_NAME:
            table.setText(row, colCustomer, data.getValue(i, colName), STYLE_CUSTOMER);
            break;

          case AR_RECEIVED:
            table.setText(row, colQuantity, renderQuantity(quantity), STYLE_QUANTITY);
            break;

          case AR_INCOME:
            table.setText(row, colIncome1, renderAmount(income1), STYLE_INCOME, STYLE_AMOUNT);
            break;

          case AR_EXPENSE:
            table.setText(row, colExpense1, renderAmount(expense1), STYLE_EXPENSE, STYLE_AMOUNT);

            table.setText(row, colProfit1, renderAmount(profit1), STYLE_PROFIT, STYLE_AMOUNT);
            table.setText(row, colMargin1, renderPercent(margin1), STYLE_MARGIN, STYLE_AMOUNT);
            break;

          case AR_SECONDARY:
            table.setText(row, colSecondary, renderQuantity(secondary),
                STYLE_SECONDARY, STYLE_QUANTITY);
            break;

          case AR_SECONDARY_INCOME:
            table.setText(row, colIncome2, renderAmount(income2), STYLE_INCOME, STYLE_AMOUNT);
            break;

          case AR_SECONDARY_EXPENSE:
            table.setText(row, colExpense2, renderAmount(expense2), STYLE_EXPENSE, STYLE_AMOUNT);

            table.setText(row, colProfit2, renderAmount(profit2), STYLE_PROFIT, STYLE_AMOUNT);
            table.setText(row, colMargin2, renderPercent(margin2), STYLE_MARGIN, STYLE_AMOUNT);
            break;
        }
      }

      totQuantity += quantity;

      if (BeeUtils.isDouble(income1)) {
        totIncome1 += income1;
      }
      if (BeeUtils.isDouble(expense1)) {
        totExpense1 += expense1;
      }

      totSecondary += secondary;

      if (BeeUtils.isDouble(income2)) {
        totIncome2 += income2;
      }
      if (BeeUtils.isDouble(expense2)) {
        totExpense2 += expense2;
      }

      table.getRowFormatter().addStyleName(row, STYLE_DETAILS);
      DomUtils.setDataIndex(table.getRow(row), i);

      row++;
    }

    if (data.getNumberOfRows() > 1) {
      table.setText(row, colQuantity, renderQuantity(totQuantity), STYLE_QUANTITY);

      table.setText(row, colIncome1, renderAmount(totIncome1), STYLE_INCOME, STYLE_AMOUNT);
      table.setText(row, colExpense1, renderAmount(totExpense1), STYLE_EXPENSE, STYLE_AMOUNT);

      double profit = profit(totIncome1, totExpense1);
      double margin = margin(profit, totIncome1);

      table.setText(row, colProfit1, renderAmount(profit), STYLE_PROFIT, STYLE_AMOUNT);
      table.setText(row, colMargin1, renderPercent(margin), STYLE_MARGIN, STYLE_AMOUNT);

      table.setText(row, colSecondary, renderQuantity(totSecondary),
          STYLE_SECONDARY, STYLE_QUANTITY);

      table.setText(row, colIncome2, renderAmount(totIncome2), STYLE_INCOME, STYLE_AMOUNT);
      table.setText(row, colExpense2, renderAmount(totExpense2), STYLE_EXPENSE, STYLE_AMOUNT);

      profit = profit(totIncome2, totExpense2);
      margin = margin(profit, totIncome2);

      table.setText(row, colProfit2, renderAmount(profit), STYLE_PROFIT, STYLE_AMOUNT);
      table.setText(row, colMargin2, renderPercent(margin), STYLE_MARGIN, STYLE_AMOUNT);

      table.getRowFormatter().addStyleName(row, STYLE_SUMMARY);
    }

    table.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        TableCellElement cellElement =
            DomUtils.getParentCell(EventUtils.getEventTargetElement(event), true);
        TableRowElement rowElement = DomUtils.getParentRow(cellElement, false);

        if (cellElement != null
            && !BeeUtils.isEmpty(cellElement.getInnerText())
            && rowElement != null && rowElement.hasClassName(STYLE_DETAILS)) {

          int dataIndex = DomUtils.getDataIndexInt(rowElement);

          if (!BeeConst.isUndef(dataIndex)) {
            boolean modal = drillModal(event.getNativeEvent());
            showDetails(data.getRow(dataIndex), cellElement, modal);
          }
        }
      }
    });

    container.add(table);
  }

  private void showDetails(SimpleRow dataRow, TableCellElement cellElement, boolean modal) {
    CompoundFilter filter = Filter.and();
    filter.add(Filter.isEqual(ALS_ORDER_STATUS, new IntegerValue(OrderStatus.COMPLETED.ordinal())));

    List<String> captions = Lists.newArrayList();

    String[] colNames = dataRow.getColumnNames();

    DateTime start = getDateTime(NAME_START_DATE);
    DateTime end = getDateTime(NAME_END_DATE);

    if (ArrayUtils.contains(colNames, BeeConst.YEAR)
        && ArrayUtils.contains(colNames, BeeConst.MONTH)) {

      Integer year = BeeUtils.unbox(dataRow.getInt(BeeConst.YEAR));
      Integer month = BeeUtils.unbox(dataRow.getInt(BeeConst.MONTH));

      if (TimeUtils.isYear(year) && TimeUtils.isMonth(month)) {
        if (start == null && end == null) {
          captions.add(BeeUtils.joinWords(year, Format.renderMonthFullStandalone(month)));
        }

        YearMonth ym = new YearMonth(year, month);

        if (start == null) {
          start = ym.getDate().getDateTime();
        }
        if (end == null) {
          end = TimeUtils.startOfNextMonth(ym).getDateTime();
        }
      }
    }

    if (start != null) {
      filter.add(Filter.isMoreEqual(COL_ORDER_DATE, new DateTimeValue(start)));
    }
    if (end != null) {
      filter.add(Filter.isLess(COL_ORDER_DATE, new DateTimeValue(end)));
    }

    if (captions.isEmpty() && (start != null || end != null)) {
      captions.add(TimeUtils.renderPeriod(start, end));
    }

    if (ArrayUtils.contains(colNames, ClassifierConstants.COL_COMPANY_PERSON)) {
      Long companyPerson = dataRow.getLong(ClassifierConstants.COL_COMPANY_PERSON);
      if (DataUtils.isId(companyPerson)) {
        filter.add(Filter.equals(ClassifierConstants.COL_COMPANY_PERSON, companyPerson));
        captions.add(BeeUtils.joinWords(dataRow.getValue(ClassifierConstants.COL_FIRST_NAME),
            dataRow.getValue(ClassifierConstants.COL_LAST_NAME)));
      }

    } else {
      String managers = getEditorValue(NAME_MANAGERS);
      if (!BeeUtils.isEmpty(managers)) {
        filter.add(Filter.any(ClassifierConstants.COL_COMPANY_PERSON,
            DataUtils.parseIdSet(managers)));
        captions.add(getFilterLabel(NAME_MANAGERS));
      }
    }

    if (ArrayUtils.contains(colNames, AdministrationConstants.COL_DEPARTMENT)) {
      Long department = dataRow.getLong(AdministrationConstants.COL_DEPARTMENT);
      if (DataUtils.isId(department)) {
        filter.add(Filter.equals(AdministrationConstants.COL_DEPARTMENT, department));
        captions.add(dataRow.getValue(AdministrationConstants.COL_DEPARTMENT_NAME));
      }

    } else {
      String departments = getEditorValue(NAME_DEPARTMENTS);
      if (!BeeUtils.isEmpty(departments)) {
        filter.add(Filter.any(AdministrationConstants.COL_DEPARTMENT,
            DataUtils.parseIdSet(departments)));
        captions.add(getFilterLabel(NAME_DEPARTMENTS));
      }
    }

    if (ArrayUtils.contains(colNames, COL_CUSTOMER)) {
      Long customer = dataRow.getLong(COL_CUSTOMER);
      if (DataUtils.isId(customer)) {
        filter.add(Filter.equals(COL_CUSTOMER, customer));
        captions.add(dataRow.getValue(ClassifierConstants.ALS_COMPANY_NAME));
      }

    } else {
      String customers = getEditorValue(NAME_CUSTOMERS);
      if (!BeeUtils.isEmpty(customers)) {
        filter.add(Filter.any(COL_CUSTOMER, DataUtils.parseIdSet(customers)));
        captions.add(getFilterLabel(NAME_CUSTOMERS));
      }
    }

    if (cellElement.hasClassName(STYLE_SECONDARY)) {
      filter.add(Filter.notNull(COL_ASSESSMENT));
      captions.add(Localized.getConstants().trAssessmentReportSecondary());
    }

    String caption = BeeUtils.notEmpty(BeeUtils.joinItems(captions),
        Localized.getConstants().trAssessmentRequests());

    drillDown(DRILL_DOWN_GRID_NAME, caption, filter, modal);
  }
}
