package com.butent.bee.client.view;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.HasDataTable;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils.ScrollBars;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.ui.WidgetCreationCallback;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.utils.XmlUtils;
import com.butent.bee.client.view.add.AddEndEvent;
import com.butent.bee.client.view.add.AddStartEvent;
import com.butent.bee.client.view.edit.EditFormEvent;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.CellGridImpl;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.navigation.PagerView;
import com.butent.bee.client.view.navigation.ScrollPager;
import com.butent.bee.client.view.search.SearchView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.ActiveRowChangeEvent;
import com.butent.bee.shared.data.event.DataRequestEvent;
import com.butent.bee.shared.data.event.ParentRowEvent;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Implements design content for a grid container component.
 */

public class GridContainerImpl extends Split implements GridContainerView, HasNavigation,
    HasSearch, ActiveRowChangeEvent.Handler, AddStartEvent.Handler, AddEndEvent.Handler,
    EditFormEvent.Handler {

  private enum Component {
    HEADER, FOOTER, SCROLLER, CONTENT
  }

  private class ExtWidget {
    private final Widget widget;

    private final Direction direction;
    private final int size;
    private final ScrollBars scrollBars;
    private final Integer splSize;

    private final Component precedes;
    private final boolean hidable;

    private ExtWidget(Widget widget, Direction direction, int size, ScrollBars scrollBars,
        Integer splSize, Component precedes, boolean hidable) {
      super();
      this.widget = widget;
      this.direction = direction;
      this.size = size;
      this.scrollBars = scrollBars;
      this.splSize = splSize;
      this.precedes = precedes;
      this.hidable = hidable;
    }

    private Direction getDirection() {
      return direction;
    }

    private ScrollBars getScrollBars() {
      return scrollBars;
    }

    private int getSize() {
      return size;
    }

    private Integer getSplSize() {
      return splSize;
    }

    private int getTotalSize() {
      return getSize() + BeeUtils.toNonNegativeInt(getSplSize());
    }

    private Widget getWidget() {
      return widget;
    }

    private boolean isHidable() {
      return hidable;
    }

    private boolean precedesFooter() {
      return Component.FOOTER.equals(precedes);
    }

    private boolean precedesHeader() {
      return Component.HEADER.equals(precedes);
    }
  }

  private static final String ATTR_PRECEDES = "precedes";
  private static final String ATTR_HIDABLE = "hidable";

  private Presenter viewPresenter = null;

  private String footerId = null;
  private String headerId = null;
  private String scrollerId = null;

  private int scrollerWidth = DomUtils.getScrollBarWidth() + 1;

  private boolean hasPaging = false;
  private boolean hasSearch = false;

  private Evaluator rowMessage = null;

  private boolean adding = false;
  private boolean enabled = true;

  private final List<ExtWidget> extWidgets = Lists.newArrayList();
  private WidgetCreationCallback extCreation = null;
  
  private IsRow lastRow = null;
  private boolean lastEnabled = false;

  public GridContainerImpl() {
    this(-1);
  }

  public GridContainerImpl(int splitterSize) {
    super(splitterSize);
  }

  public GridContainerImpl(String style) {
    this(style, -1);
  }

  public GridContainerImpl(String style, int splitterSize) {
    super(style, splitterSize);
  }

  public void bind() {
    if (hasHeader()) {
      getContent().getGrid().addLoadingStateChangeHandler(getHeader());
    }
    if (hasFooter()) {
      getContent().getGrid().addSelectionCountChangeHandler(getFooter());
    }

    getContent().getGrid().addActiveRowChangeHandler(this);

    getContent().addAddStartHandler(this);
    getContent().addAddEndHandler(this);

    getContent().addEditFormHandler(this);
  }

  public void create(GridDescription gridDescription, List<BeeColumn> dataColumns, int rowCount,
      BeeRowSet rowSet, GridCallback gridCallback, Collection<UiOption> options) {

    int minRows = BeeUtils.unbox(gridDescription.getPagingThreshold());
    setHasPaging(UiOption.hasPaging(options) && rowCount >= minRows);

    minRows = BeeUtils.unbox(gridDescription.getSearchThreshold());
    setHasSearch(UiOption.hasSearch(options) && rowCount >= minRows);

    boolean readOnly = BeeUtils.isTrue(gridDescription.isReadOnly());

    HeaderView header;
    if (UiOption.hasHeader(options)) {
      header = new HeaderImpl();
      String caption = (gridCallback == null) ? null : gridCallback.getCaption();
      if (caption == null) {
        caption = gridDescription.getCaption();
      }
      header.create(caption, !BeeUtils.isEmpty(gridDescription.getViewName()), readOnly, options,
          gridDescription.getEnabledActions(), gridDescription.getDisabledActions());
    } else {
      header = null;
    }

    String name = gridDescription.getName();

    GridView content = new CellGridImpl(name);
    content.create(dataColumns, rowCount, rowSet, gridDescription, gridCallback, hasSearch());

    FooterView footer;
    ScrollPager scroller;

    if (hasPaging() || hasSearch()) {
      footer = new FooterImpl();
      footer.create(rowCount, hasPaging(), true, hasSearch());
    } else {
      footer = null;
    }

    if (hasPaging()) {
      scroller = new ScrollPager();
    } else {
      scroller = null;
    }

    getExtWidgets().clear();
    if (gridDescription.hasWidgets()) {
      for (String xml : gridDescription.getWidgets()) {
        ExtWidget extWidget = createExtWidget(xml, gridCallback);
        if (extWidget != null) {
          getExtWidgets().add(extWidget);
        }
      }
    }

    addExtWidgets(Component.HEADER);
    if (header != null) {
      addNorth(header.asWidget(), header.getHeight());
      setHeaderId(header.getWidgetId());
    }

    addExtWidgets(Component.FOOTER);
    if (footer != null) {
      addSouth(footer.asWidget(), footer.getHeight());
      setFooterId(footer.getWidgetId());
    }

    addExtWidgets(null);
    if (scroller != null) {
      addEast(scroller, getScrollerWidth());
      setScrollerId(scroller.getWidgetId());
      add(content.asWidget(), ScrollBars.HORIZONTAL);
      sinkEvents(Event.ONMOUSEWHEEL);
    } else {
      add(content.asWidget(), ScrollBars.BOTH);
    }

    if (gridDescription.getRowMessage() != null) {
      setRowMessage(Evaluator.create(gridDescription.getRowMessage(), null, dataColumns));
    }
    
    if (getExtCreation() != null) {
      getExtCreation().addBinding(name, getId(), gridDescription.getParent());
      getExtCreation().bind(this, getId());
    }
  }

  public GridView getContent() {
    if (getCenter() == null) {
      return null;
    }
    return (GridView) getCenter();
  }

  public HeaderView getHeader() {
    if (BeeUtils.isEmpty(getHeaderId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof HeaderView
          && BeeUtils.same(widget.getElement().getId(), getHeaderId())) {
        return (HeaderView) widget;
      }
    }
    return null;
  }

  @Override
  public String getIdPrefix() {
    return "grid-container";
  }

  public Collection<PagerView> getPagers() {
    if (hasPaging()) {
      return ViewHelper.getPagers(this);
    } else {
      return null;
    }
  }

  public Collection<SearchView> getSearchers() {
    if (hasSearch()) {
      return ViewHelper.getSearchers(this);
    } else {
      return null;
    }
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void onActiveRowChange(ActiveRowChangeEvent event) {
    IsRow rowValue = event.getRowValue();

    boolean rowEnabled;
    if (rowValue == null) {
      rowEnabled = false;
    } else {
      GridView gridView = getContent();
      rowEnabled = !gridView.isReadOnly() && isEnabled() && gridView.isEnabled()
          && gridView.isRowEditable(rowValue, false);
    }
    
    if (DataUtils.same(rowValue, getLastRow()) && rowEnabled == wasLastEnabled()) {
      return;
    }

    if (getRowMessage() != null) {
      getRowMessage().update(event.getRowValue());
      String message = getRowMessage().evaluate();

      if (hasHeader()) {
        getHeader().setMessage(message);
      }
    }
    
    String eventSource = BeeUtils.ifString(getViewPresenter().getEventSource(), getId());
    BeeKeeper.getBus().fireEventFromSource(new ParentRowEvent(rowValue, rowEnabled), eventSource);
    
    setLastRow(rowValue);
    setLastEnabled(rowEnabled);
  }

  public void onAddEnd(AddEndEvent event) {
    if (!event.isPopup()) {
      showChildren(true);
    }
    setAdding(false);
  }

  public void onAddStart(AddStartEvent event) {
    setAdding(true);
    if (!event.isPopup()) {
      showChildren(false);
    }
  }

  @Override
  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    if (isAdding() || !isEnabled()) {
      return;
    }

    if (event.getTypeInt() == Event.ONMOUSEWHEEL) {
      int y = event.getMouseWheelVelocityY();

      HasDataTable display = getContent().getGrid();
      ScrollPager scroller = getScroller();

      if (y == 0 || display == null || scroller == null) {
        return;
      }
      if (display.isEditing()) {
        return;
      }

      EventTarget target = event.getEventTarget();
      if (target != null && scroller.getElement().isOrHasChild(Node.as(target))) {
        return;
      }

      event.preventDefault();
      if (EventUtils.isInputElement(target)) {
        return;
      }

      int rc = display.getRowCount();
      int start = display.getPageStart();
      int length = display.getPageSize();

      if (length > 0 && rc > length) {
        int p = -1;
        if (y > 0 && start + length < rc) {
          p = start + 1;
        } else if (y < 0 && start > 0) {
          p = start - 1;
        }

        if (p >= 0) {
          display.setPageStart(p, true, true);
        }
      }
    }
  }

  public void onEditForm(EditFormEvent event) {
    if (event.isOpening() && !event.isPopup()) {
      showChildren(false);
    } else if (event.isClosing() && !event.isPopup()) {
      showChildren(true);
    }
  }

  @Override
  public void onResize() {
    if (isAttached() && providesResize()) {
      super.onResize();
      getContent().getGrid().updatePageSize();
    }
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;
    DomUtils.enableChildren(this, enabled);
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
    for (Widget widget : getChildren()) {
      if (widget instanceof View && ((View) widget).getViewPresenter() == null) {
        ((View) widget).setViewPresenter(viewPresenter);
      }
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public void execute() {
        CellGrid grid = getContent().getGrid();
        if (!hasPaging()) {
          grid.refresh(true);
          return;
        }

        Collection<PagerView> pagers = getPagers();
        if (pagers != null) {
          for (PagerView pager : pagers) {
            pager.start(grid);
          }
        }

        int ps = estimatePageSize();
        grid.setPageSize(ps, true, false);

        int ds = grid.getDataSize();
        if (ps > 0 && ps < ds) {
          grid.getRowData().subList(ps, ds).clear();
          grid.refresh(true);
        } else if (ps > 0 && ps > ds && ds < grid.getRowCount()) {
          DataRequestEvent.fire(grid);
        } else {
          grid.refresh(true);
        }
      }
    });
  }

  @Override
  protected void onUnload() {
    if (!BeeKeeper.getScreen().isTemporaryDetach() && getViewPresenter() != null) {
      getViewPresenter().onViewUnload();
    }
    super.onUnload();
  }

  private void addExtWidgets(Component before) {
    if (getExtWidgets().isEmpty()) {
      return;
    }

    boolean head = Component.HEADER.equals(before);
    boolean foot = Component.FOOTER.equals(before);

    boolean ok;
    for (ExtWidget extWidget : getExtWidgets()) {
      if (extWidget.precedesHeader()) {
        ok = head;
      } else if (extWidget.precedesFooter()) {
        ok = foot;
      } else {
        ok = !head && !foot;
      }

      if (ok) {
        add(extWidget.getWidget(), extWidget.getDirection(), extWidget.getSize(),
            extWidget.getScrollBars(), extWidget.getSplSize());
      }
    }
  }

  private ExtWidget createExtWidget(String xml, GridCallback gridCallback) {
    Document doc = XmlUtils.parse(xml);
    if (doc == null) {
      return null;
    }

    Element root = doc.getDocumentElement();
    if (root == null) {
      BeeKeeper.getLog().severe("ext widget: document element not found", xml);
      return null;
    }
    if (gridCallback != null && !gridCallback.onLoadExtWidget(root)) {
      return null;
    }

    String tagName = root.getTagName();
    Direction direction = BeeUtils.getConstant(Direction.class, tagName);
    if (!validDirection(direction, false)) {
      BeeKeeper.getLog().severe("ext widget: invalid root tag name", BeeUtils.quote(tagName));
      return null;
    }

    int size = BeeUtils.unbox(XmlUtils.getAttributeInteger(root, FormWidget.ATTR_SIZE));
    if (size <= 0) {
      BeeKeeper.getLog().severe("ext widget size must be positive integer");
      return null;
    }
    
    if (getExtCreation() == null) {
      setExtCreation(new WidgetCreationCallback());
    }
    Widget widget = FormFactory.createWidget(root, null, getExtCreation(), gridCallback,
        "create ext widget:");
    if (widget == null) {
      return null;
    }

    ScrollBars scrollBars = XmlUtils.getAttributeScrollBars(root, FormWidget.ATTR_SCROLL_BARS,
        ScrollBars.BOTH);
    Integer splSize = XmlUtils.getAttributeInteger(root, FormWidget.ATTR_SPLITTER_SIZE);

    Component precedes = BeeUtils.getConstant(Component.class, root.getAttribute(ATTR_PRECEDES));
    boolean hidable = !BeeUtils.isFalse(XmlUtils.getAttributeBoolean(root, ATTR_HIDABLE));

    return new ExtWidget(widget, direction, size, scrollBars, splSize, precedes, hidable);
  }

  private int estimatePageSize() {
    if (hasPaging()) {
      int w = getElement().getClientWidth();
      int h = getElement().getClientHeight();

      if (w <= 0) {
        w = DomUtils.getParentClientWidth(this);
      }
      if (h <= 0) {
        h = DomUtils.getParentClientHeight(this);
      }

      return estimatePageSize(getContent(), w, h);
    }
    return BeeConst.UNDEF;
  }

  private int estimatePageSize(GridView content, int containerWidth, int containerHeight) {
    if (content != null && containerHeight > 0) {
      int w = containerWidth;
      int h = containerHeight;

      if (hasHeader()) {
        h -= getHeader().getHeight();
      }
      if (hasFooter()) {
        h -= getFooter().getHeight();
      }
      if (hasScroller()) {
        w -= getScrollerWidth();
      }

      for (ExtWidget extWidget : getExtWidgets()) {
        if (extWidget.getDirection().isHorizontal()) {
          w -= extWidget.getTotalSize();
        } else {
          h -= extWidget.getTotalSize();
        }
      }

      return content.estimatePageSize(w, h);
    }
    return BeeConst.UNDEF;
  }

  private WidgetCreationCallback getExtCreation() {
    return extCreation;
  }

  private List<ExtWidget> getExtWidgets() {
    return extWidgets;
  }

  private FooterView getFooter() {
    if (BeeUtils.isEmpty(getFooterId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof FooterView
          && BeeUtils.same(widget.getElement().getId(), getFooterId())) {
        return (FooterView) widget;
      }
    }
    return null;
  }

  private String getFooterId() {
    return footerId;
  }

  private String getHeaderId() {
    return headerId;
  }

  private IsRow getLastRow() {
    return lastRow;
  }

  private Evaluator getRowMessage() {
    return rowMessage;
  }

  private ScrollPager getScroller() {
    if (BeeUtils.isEmpty(getScrollerId())) {
      return null;
    }
    for (Widget widget : getChildren()) {
      if (widget instanceof ScrollPager
          && BeeUtils.same(widget.getElement().getId(), getScrollerId())) {
        return (ScrollPager) widget;
      }
    }
    return null;
  }

  private String getScrollerId() {
    return scrollerId;
  }

  private int getScrollerWidth() {
    return scrollerWidth;
  }

  private boolean hasFooter() {
    return !BeeUtils.isEmpty(getFooterId());
  }

  private boolean hasHeader() {
    return !BeeUtils.isEmpty(getHeaderId());
  }

  private boolean hasPaging() {
    return hasPaging;
  }

  private boolean hasScroller() {
    return !BeeUtils.isEmpty(getScrollerId());
  }

  private boolean hasSearch() {
    return hasSearch;
  }

  private boolean isAdding() {
    return adding;
  }

  private void setAdding(boolean adding) {
    this.adding = adding;
  }

  private void setExtCreation(WidgetCreationCallback extCreation) {
    this.extCreation = extCreation;
  }

  private void setFooterId(String footerId) {
    this.footerId = footerId;
  }

  private void setHasPaging(boolean hasPaging) {
    this.hasPaging = hasPaging;
  }

  private void setHasSearch(boolean hasSearch) {
    this.hasSearch = hasSearch;
  }

  private void setHeaderId(String headerId) {
    this.headerId = headerId;
  }

  private void setLastEnabled(boolean lastEnabled) {
    this.lastEnabled = lastEnabled;
  }

  private void setLastRow(IsRow lastRow) {
    this.lastRow = lastRow;
  }

  private void setRowMessage(Evaluator rowMessage) {
    this.rowMessage = rowMessage;
  }

  private void setScrollerId(String scrollerId) {
    this.scrollerId = scrollerId;
  }

  private void showChildren(boolean show) {
    if (!show) {
      setProvidesResize(false);
    }

    if (hasHeader()) {
      HeaderView header = getHeader();
      if (header != null) {
        setWidgetSize(header.asWidget(), show ? header.getHeight() : 0);
      }
    }
    if (hasFooter()) {
      FooterView footer = getFooter();
      if (footer != null) {
        setWidgetSize(footer.asWidget(), show ? footer.getHeight() : 0);
      }
    }
    if (hasScroller()) {
      ScrollPager scroller = getScroller();
      if (scroller != null) {
        setWidgetSize(scroller, show ? getScrollerWidth() : 0);
      }
    }

    for (ExtWidget extWidget : getExtWidgets()) {
      if (extWidget.isHidable()) {
        setWidgetSize(extWidget.getWidget(), show ? extWidget.getSize() : 0);
      }
    }
    
    if (show) {
      setProvidesResize(true);
    }
  }

  private boolean wasLastEnabled() {
    return lastEnabled;
  }
}
