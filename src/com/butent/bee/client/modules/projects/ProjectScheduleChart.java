package com.butent.bee.client.modules.projects;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.timeboard.TimeBoard;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.client.timeboard.TimeBoardRowLayout;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewCallback;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Size;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.event.ModificationEvent;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class ProjectScheduleChart extends TimeBoard {

  public static final String SUPPLIER_KEY = "project_schedule_chart";

  public static void open(final Flow widget, long projectId) {
    ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_PROJECT_CHART_DATA);
    params.addDataItem(VAR_PROJECT, BeeUtils.toString(projectId));

    BeeKeeper.getRpc().makePostRequest(params, getResponseCallback(getViewCallback(widget),
        projectId));
  }

  private static final String STYLE_PREFIX = ProjectsKeeper.STYLE_PREFIX + "chart-";
  private static final String STYLE_STAGE_ROW = STYLE_PREFIX + "stage-row";
  private static final String STYLE_STAGE_FLOW = STYLE_PREFIX + "stage-flow";
  private static final String STYLE_STAGE_LABEL = STYLE_PREFIX + "stage-row-label";
  private static final String STYLE_STAGE_ROW_SEPARATOR = STYLE_PREFIX + "stage-row-separator";
  private static final String STYLE_DATA_ROW_SEPARATOR = STYLE_PREFIX + "data-row-separator";

  private static final int DEFAULT_CHART_LEFT = 300;
  private static final int DEFAUT_DAY_WIDTH = 30;
  private static final int DEFAULT_ROW_HEIGHT = 30;

  private final class ChartItem implements HasDateRange {

    private JustDate start;
    private JustDate end;
    private Long stageId;
    private String caption;
    private String color;
    private String viewName;
    private String taskStatus;
    private String beeRowSet;

    @Override
    public Range<JustDate> getRange() {
      JustDate s = start;
      JustDate e = end;

      if (s == null && e == null) {
        return null;
      } else if (s == null && e != null) {
        s = e;
      } else if (s != null && e == null) {
        e = s;
      }

      return Range.closed(s, BeeUtils.max(s, e));
    }

    private ChartItem(String viewName, Long stageId, String caption, JustDate start,
        JustDate end, String color, String taskStatus, String beeRowSet) {

      this();
      this.viewName = viewName;
      this.start = start;
      this.end = end;
      this.caption = caption;
      this.color = color;
      this.stageId = stageId;
      this.taskStatus = taskStatus;
      this.beeRowSet = beeRowSet;
    }

    private Long getStageId() {
      return stageId;
    }

    private String getCaption() {
      return caption;
    }

    private String getColor() {
      return color;
    }

    private String getViewName() {
      return viewName;
    }

    private ChartItem() {
    }

    private String getTaskStatus() {
      return taskStatus;
    }
  }

  private final List<ChartItem> chartItems = Lists.newArrayList();
  private Long projectId;
  private final Set<String> relevantDataViews = Sets.newHashSet(VIEW_PROJECT_DATES,
      VIEW_PROJECT_STAGES, TaskConstants.VIEW_TASKS, TaskConstants.VIEW_RECURRING_TASKS);
  private int dataWidth = DEFAULT_CHART_LEFT / 2;
  private int stageWidth = DEFAULT_CHART_LEFT / 2;

  @Override
  public String getCaption() {
    return Localized.dictionary().prjSchedule();
  }

  @Override
  public String getSupplierKey() {
    return SUPPLIER_KEY;
  }

  @Override
  protected void editSettings() {
  }

  @Override
  protected Collection<? extends HasDateRange> getChartItems() {
    return chartItems;
  }

  @Override
  protected Set<Action> getEnabledActions() {
    return EnumSet.of(Action.REFRESH, Action.PRINT);
  }

  @Override
  protected String getFooterHeightColumnName() {
    return COL_PSC_FOOTER_HEIGHT;
  }

  @Override
  protected String getHeaderHeightColumnName() {
    return COL_PSC_HEADER_HEIGHT;
  }

  @Override
  protected Set<Action> getHiddenActions() {
    return EnumSet.of(Action.CLOSE);
  }

  @Override
  protected String getRowHeightColumnName() {
    return COL_PSC_ROW_HEIGHT;
  }

  @Override
  protected int getRowHeight() {
    return DEFAULT_ROW_HEIGHT;
  }

  @Override
  protected String getStripOpacityColumnName() {
    return COL_PSC_STRIP_OPACITY;
  }

  @Override
  public void handleAction(Action action) {
    if (action.equals(Action.CLOSE)) {
      return;
    }
    super.handleAction(action);
  }

  @Override
  protected boolean isDataEventRelevant(ModificationEvent<?> event) {
    return event != null && event.containsAny(relevantDataViews);
  }

  @Override
  protected void prepareChart(Size canvasSize) {
    setChartLeft(stageWidth + dataWidth);
    setDayColumnWidth(DEFAUT_DAY_WIDTH);
    setChartWidth(canvasSize.getWidth() - getChartLeft() - getChartRight());

  }

  @Override
  protected void refresh() {
    ParameterList params = ProjectsKeeper.createSvcArgs(SVC_GET_PROJECT_CHART_DATA);
    params.addDataItem(VAR_PROJECT, BeeUtils.toString(projectId));

    BeeKeeper.getRpc().makePostRequest(params, response -> {
      if (setData(response, false)) {
        render(true);
      }
    });
  }

  @Override
  protected void renderContent(ComplexPanel panel) {
    List<TimeBoardRowLayout> boardLayout = createLayout();

    int rc = TimeBoardRowLayout.countRows(boardLayout, 1);

    initContent(panel, rc);

    if (boardLayout.isEmpty()) {
      return;
    }

    int rowIndex = 0;
    int calendarWidth = getCalendarWidth();
    Edges margins = new Edges();
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    IdentifiableWidget stageWidget = null;
    IdentifiableWidget flowWidget = null;
    int stageStartRow = 0;
    int flowStartRow = 0;

    Long lastStage = null;
    String lastFlow = null;

    Range<JustDate> range = getVisibleRange();

    for (TimeBoardRowLayout layout : boardLayout) {
      int chartRowIndex = layout.getDataIndex();
      ChartItem dataItem = chartItems.get(chartRowIndex);

      int top = rowIndex * getRowHeight();

      if (rowIndex == 0) {
        stageWidget = createChartRowWidget(dataItem, true);
        stageStartRow = rowIndex;

        lastStage = dataItem.getStageId();

        // second level widgets
        flowWidget = createChartRowWidget(dataItem, false);
        flowStartRow = rowIndex;

        lastFlow = BeeUtils.joinItems(dataItem.getViewName(), rowIndex);

      } else {
        boolean stageChanged = !Objects.equals(lastStage, dataItem.getStageId());
        // second level changed
        boolean flowChanged = stageChanged || !Objects.equals(lastFlow,
            BeeUtils.joinItems(dataItem.getViewName(), rowIndex));

        if (stageChanged) {
          addChartRowWidget(panel, stageWidget, stageStartRow, rowIndex - 1, 0);
          stageWidget = createChartRowWidget(dataItem, true);
          stageStartRow = rowIndex;
          lastStage = dataItem.getStageId();
        }

        // if second level change
        if (flowChanged) {
          addChartRowWidget(panel, flowWidget, flowStartRow, rowIndex - 1, 1);
          flowWidget = createChartRowWidget(dataItem, false);
          flowStartRow = rowIndex;
          lastFlow = BeeUtils.joinItems(dataItem.getViewName(), rowIndex);
        }

        if (stageChanged) {
          TimeBoardHelper.addRowSeparator(panel, STYLE_STAGE_ROW_SEPARATOR, top, 0,
              getChartLeft() + calendarWidth);
        } else if (flowChanged) {
          TimeBoardHelper.addRowSeparator(panel, STYLE_DATA_ROW_SEPARATOR, top,
              stageWidth, dataWidth + calendarWidth);
        }
      }

      for (ChartItem item : chartItems) {
        // first level filter
        if (item.getStageId() == dataItem.stageId
            && BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())
            && BeeUtils.same(item.getViewName(), dataItem.getViewName())) {

          if (TimeBoardHelper.isActive(item, range) && item.getRange() != null) {
            Widget itemWidget = createItemWidget(item);
            Rectangle rectangle = getRectangle(item.getRange(), rowIndex);
            TimeBoardHelper.apply(itemWidget, rectangle, margins);
            styleItemWidget(item, itemWidget);
            panel.add(itemWidget);
          }
        }

        // second level filter
        if (BeeUtils.same(dataItem.getViewName(), item.getViewName())
            && BeeUtils.same(dataItem.getCaption(), item.getCaption())
            && !BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())) {

          if (TimeBoardHelper.isActive(item, range)) {
            Widget itemWidget = createItemWidget(item);
            Rectangle rectangle = getRectangle(item.getRange(), rowIndex);
            TimeBoardHelper.apply(itemWidget, rectangle, margins);
            styleItemWidget(item, itemWidget);
            panel.add(itemWidget);
          }
        }
      }

      int lastRow = boardLayout.size() - 1;

      if (stageWidget != null) {
        addChartRowWidget(panel, stageWidget, stageStartRow, lastRow, 0);
      }

      // second level
      if (flowWidget != null) {
        addChartRowWidget(panel, flowWidget, flowStartRow, lastRow, 1);
      }

      rowIndex++;
    }

  }

  @Override
  protected void renderMovers(ComplexPanel panel, int height) {
    Mover stageMover = TimeBoardHelper.createHorizontalMover();
    StyleUtils.setLeft(stageMover, stageWidth - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(stageMover, height);

    panel.add(stageMover);

    Mover dataMover = TimeBoardHelper.createHorizontalMover();
    StyleUtils.setLeft(dataMover, getChartLeft() - TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    StyleUtils.setHeight(dataMover, height);

    dataMover.addMoveHandler(this::onDataResize);

    panel.add(dataMover);

  }

  @Override
  protected boolean setData(ResponseObject response, boolean init) {
    getCanvas().clear();
    chartItems.clear();
    if (response.isEmpty()) {
      return false;
    }

    if (!response.hasResponse(SimpleRowSet.class)) {
      Assert.notImplemented();
      return false;
    }

    SimpleRowSet rs = SimpleRowSet.restore(response.getResponseAsString());

    int idxViewName = rs.getColumnIndex(ALS_VIEW_NAME);
    int idxStage = rs.getColumnIndex(ALS_CHART_ID);
    int idxCaption = rs.getColumnIndex(ALS_CHART_CAPTION);
    int idxStart = rs.getColumnIndex(ALS_CHART_START);
    int idxEnd = rs.getColumnIndex(ALS_CHART_END);
    int idxColor = rs.getColumnIndex(ALS_CHART_FLOW_COLOR);
    int idxStatus = rs.getColumnIndex(ALS_TASK_STATUS);
    int idxBeeRowSet = rs.getColumnIndex(PROP_RS);

    for (String[] row : rs.getRows()) {
      Long id = BeeUtils.toLong(row[idxStage]);
      JustDate start = null;
      JustDate end = null;
      String taskStatus = row[idxStatus];

      if (!BeeUtils.isEmpty(row[idxStart])) {
        start = new JustDate(BeeUtils.toInt(row[idxStart]));
      }

      if (!BeeUtils.isEmpty(row[idxEnd])) {
        end = new JustDate(BeeUtils.toInt(row[idxEnd]));
      }

      ChartItem ci = new ChartItem(row[idxViewName], id, row[idxCaption], start,
          end, row[idxColor], taskStatus, row[idxBeeRowSet]);

      if (ci.getRange() != null) {
        chartItems.add(ci);
      }
    }

    updateMaxRange();

    return true;
  }

  private static ResponseCallback getResponseCallback(final ViewCallback viewCallback,
      final Long projectId) {
    return response -> {
      ProjectScheduleChart chart = new ProjectScheduleChart(projectId);
      chart.onCreate(response, viewCallback);
    };
  }

  private static ViewCallback getViewCallback(final Flow widget) {
    return widget::add;
  }

  private void addChartRowWidget(HasWidgets panel, IdentifiableWidget widget,
      int firstRow, int lastRow, int level) {

    Rectangle rectangle =
        TimeBoardHelper
            .getRectangle(level * stageWidth, level > 0 ? dataWidth : stageWidth,
                firstRow,
                lastRow, getRowHeight());

    Edges margins = new Edges();
    margins.setRight(TimeBoardHelper.DEFAULT_MOVER_WIDTH);
    margins.setBottom(TimeBoardHelper.ROW_SEPARATOR_HEIGHT);

    TimeBoardHelper.apply(widget.asWidget(), rectangle, margins);

    panel.add(widget.asWidget());
  }

  private static IdentifiableWidget createChartRowWidget(ChartItem item, boolean firstLevel) {
    Flow panel = new Flow(STYLE_STAGE_ROW);
    final CustomDiv label = new CustomDiv(STYLE_STAGE_LABEL);

    if (firstLevel && BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())) {
      label.setText(item.getCaption());
    } else if (firstLevel) {
      label.setText(Localized.dictionary().project());
    } else if (!firstLevel && !BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())) {
      label.setText(item.getCaption());
    } else {
      label.setText(BeeConst.STRING_EMPTY);
    }

    final BeeRowSet rs = BeeRowSet.restore(item.beeRowSet);

    label.addClickHandler(arg0 -> RowEditor.open(rs.getViewName(), rs.getRow(0).getId()));

    panel.add(label);
    return panel;
  }

  private List<TimeBoardRowLayout> createLayout() {
    List<TimeBoardRowLayout> result = new ArrayList<>();

    Range<JustDate> range = getVisibleRange();
    List<HasDateRange> items;
    List<ChartItem> usedItems = new ArrayList<>();

    for (int i = 0; i < chartItems.size(); i++) {
      TimeBoardRowLayout layout = new TimeBoardRowLayout(i);
      List<HasDateRange> filterItems = Lists.newArrayList();
      for (ChartItem item : chartItems) {
        // first level filter
        if (item.getStageId() == chartItems.get(i).getStageId()
            && BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())
            && BeeUtils.same(item.getViewName(), chartItems.get(i).getViewName())) {
          filterItems.add(item);
        }

        // second level filter
        if (BeeUtils.same(chartItems.get(i).getViewName(), item.getViewName())
            && BeeUtils.same(chartItems.get(i).getCaption(), item.getCaption())
            && !usedItems.contains(item)
            && !BeeUtils.same(VIEW_PROJECT_STAGES, item.getViewName())) {
          filterItems.add(item);
          usedItems.add(item);
        }
      }

      if (filterItems.size() > 0) {
        items = TimeBoardHelper.getActiveItems(filterItems, range);
        layout.addItems(Long.valueOf(chartItems.get(i).getStageId()), items, range);
        result.add(layout);
      }

    }

    return result;
  }

  private static Widget createItemWidget(ChartItem item) {
    Flow panel = new Flow(STYLE_STAGE_ROW);
    addClickHandler(panel, item.viewName, BeeRowSet.restore(item.beeRowSet).getRow(0).getId());
    panel.addStyleName(STYLE_STAGE_FLOW);
    panel.addStyleName(STYLE_STAGE_FLOW + BeeConst.STRING_MINUS + item.getViewName());

    if (BeeUtils.same(item.getViewName(), TaskConstants.VIEW_TASKS)
        && !BeeUtils.isEmpty(item.getTaskStatus())) {
      panel.addStyleName(STYLE_STAGE_FLOW + BeeConst.STRING_MINUS + item.getViewName()
          + BeeConst.STRING_MINUS + item.getTaskStatus());
    }

    panel.setTitle(item.getCaption() + BeeConst.STRING_EOL + item.getRange().toString());
    if (!BeeUtils.isEmpty(item.getColor())) {
      StyleUtils.setBackgroundColor(panel, item.getColor());
    }
    return panel;
  }

  private ProjectScheduleChart(Long projectId) {
    this.projectId = projectId;
  }

  private static void addClickHandler(Flow panel, final String viewName, final Long rowId) {
    panel.addClickHandler(arg0 -> RowEditor.open(viewName, rowId));
  }

  private void onDataResize(MoveEvent event) {
    int delta = event.getDeltaX();

    Element resizer = ((Mover) event.getSource()).getElement();

    int oldLeft = StyleUtils.getLeft(resizer);
    int maxLeft = getLastResizableColumnMaxLeft(DEFAULT_CHART_LEFT);
    int newLeft = BeeUtils.clamp(oldLeft + delta, stageWidth, maxLeft);

    if (newLeft != oldLeft || event.isFinished()) {
      if (newLeft != oldLeft) {
        StyleUtils.setLeft(resizer, newLeft);
      }

      int px = newLeft + TimeBoardHelper.DEFAULT_MOVER_WIDTH - stageWidth;

      if (event.isFinished()) {
        dataWidth = px;
        render(false);
      }
    }
  }
}
