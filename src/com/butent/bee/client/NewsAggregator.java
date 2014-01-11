package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.commons.UserFeedsInterceptor;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.widget.Badge;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.news.Feed;
import com.butent.bee.shared.news.Headline;
import com.butent.bee.shared.news.NewsConstants;
import com.butent.bee.shared.news.Subscription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsAggregator implements HandlesAllDataEvents {

  private final class HeadlinePanel extends Flow {

    private static final String STYLE_HEADLINE_PREFIX = STYLE_PREFIX + "headline-";
    private static final String STYLE_NEW = STYLE_HEADLINE_PREFIX + "new";
    private static final String STYLE_UPD = STYLE_HEADLINE_PREFIX + "upd";

    private final long dataId;
    private final boolean isNew;

    private HeadlinePanel(Headline headline) {
      super(STYLE_HEADLINE_PREFIX + "panel");

      this.dataId = headline.getId();
      this.isNew = headline.isNew();

      CustomDiv typeWidget = new CustomDiv(headline.isNew() ? STYLE_NEW : STYLE_UPD);
      add(typeWidget);

      Label captionWidget = new Label(headline.getCaption());
      captionWidget.addStyleName(STYLE_HEADLINE_PREFIX + "caption");

      if (!BeeUtils.isEmpty(headline.getSubtitle())) {
        captionWidget.setTitle(headline.getSubtitle());
      }

      captionWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          readHeadline(HeadlinePanel.this);
        }
      });

      add(captionWidget);

      Label dismiss = new Label(String.valueOf(BeeConst.CHAR_TIMES));
      dismiss.addStyleName(STYLE_HEADLINE_PREFIX + "dismiss");

      dismiss.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dismissHeadline(HeadlinePanel.this);
        }
      });

      add(dismiss);
    }

    private long getDataId() {
      return dataId;
    }

    private Feed getFeed() {
      for (Widget parent = getParent(); parent != null; parent = parent.getParent()) {
        if (parent instanceof SubscriptionPanel) {
          return ((SubscriptionPanel) parent).getFeed();
        }
      }
      return null;
    }
    
    private boolean isNew() {
      return isNew;
    }
  }

  private final class NewsPanel extends Flow {

    private static final String STYLE_LOADING = STYLE_PREFIX + "loading";
    private static final String STYLE_NOT_LOADING = STYLE_PREFIX + "not-loading";

    private static final String STYLE_EMPTY = STYLE_PREFIX + "empty";

    private final FaLabel disclosureWidget;
    private final FaLabel loadingWidget;

    private final Flow content;

    private boolean open;

    private NewsPanel() {
      super(STYLE_PREFIX + "panel");
      addStyleName(STYLE_EMPTY);

      Flow header = new Flow(STYLE_PREFIX + "header");

      this.disclosureWidget = new FaLabel(FontAwesome.CARET_RIGHT);
      disclosureWidget.addStyleName(STYLE_PREFIX + "disclosure");

      disclosureWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleOpen();
        }
      });

      header.add(disclosureWidget);

      FaLabel refreshWidget = new FaLabel(FontAwesome.REFRESH);
      refreshWidget.setTitle(Localized.getConstants().actionRefresh());
      refreshWidget.addStyleName(STYLE_PREFIX + "refresh");

      refreshWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          refresh();
        }
      });

      header.add(refreshWidget);

      FaLabel settingsWidget = new FaLabel(FontAwesome.GEAR);
      settingsWidget.setTitle(Localized.getConstants().actionConfigure());
      settingsWidget.addStyleName(STYLE_PREFIX + "settings");

      settingsWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          UserFeedsInterceptor interceptor =
              new UserFeedsInterceptor(BeeKeeper.getUser().getUserId());
          GridOptions gridOptions = GridOptions.forCurrentUserFilter(NewsConstants.COL_UF_USER);

          GridFactory.openGrid(NewsConstants.GRID_USER_FEEDS, interceptor, gridOptions);
        }
      });

      header.add(settingsWidget);

      this.loadingWidget = new FaLabel(FontAwesome.SPINNER);
      loadingWidget.addStyleName(STYLE_NOT_LOADING);

      header.add(loadingWidget);

      add(header);

      this.content = new Flow(STYLE_PREFIX + "content");
      add(content);
    }

    private void addSubscriptionPanel(SubscriptionPanel subscriptionPanel) {
      if (content.isEmpty()) {
        removeStyleName(STYLE_EMPTY);
      }
      content.add(subscriptionPanel);
    }

    private SubscriptionPanel asSubscriptionPanel(Widget widget) {
      if (widget instanceof SubscriptionPanel) {
        return (SubscriptionPanel) widget;
      } else {
        return null;
      }
    }

    private void clearSubscriptions() {
      if (!content.isEmpty()) {
        content.clear();
        addStyleName(STYLE_EMPTY);
      }
    }

    private void endRefresh() {
      loadingWidget.addStyleName(STYLE_NOT_LOADING);
      loadingWidget.removeStyleName(STYLE_LOADING);
    }

    private SubscriptionPanel findSubscriptionPanel(Feed feed) {
      for (Widget widget : content) {
        SubscriptionPanel subscriptionPanel = asSubscriptionPanel(widget);
        if (subscriptionPanel != null && subscriptionPanel.getFeed() == feed) {
          return subscriptionPanel;
        }
      }
      return null;
    }

    private boolean isOpen() {
      return open;
    }

    private boolean removeHeadline(Feed feed, long dataId) {
      SubscriptionPanel subscriptionPanel = findSubscriptionPanel(feed);

      if (subscriptionPanel == null) {
        return false;

      } else {
        boolean ok = subscriptionPanel.removeHeadline(dataId);

        if (ok && !subscriptionPanel.hasHeadlines()) {
          content.remove(subscriptionPanel);
          if (content.isEmpty()) {
            addStyleName(STYLE_EMPTY);
          }
        }

        return ok;
      }
    }

    private void setOpen(boolean open) {
      this.open = open;
    }

    private void startRefresh() {
      loadingWidget.addStyleName(STYLE_LOADING);
      loadingWidget.removeStyleName(STYLE_NOT_LOADING);
    }

    private void toggleOpen() {
      setOpen(!isOpen());
      disclosureWidget.setChar(isOpen() ? FontAwesome.CARET_DOWN : FontAwesome.CARET_RIGHT);

      for (Widget widget : content) {
        SubscriptionPanel subscriptionPanel = asSubscriptionPanel(widget);
        if (subscriptionPanel != null && subscriptionPanel.isOpen() != isOpen()) {
          subscriptionPanel.toggleOpen();
        }
      }
    }
  }

  private final class SubscriptionPanel extends Flow {

    private static final String STYLE_SUBSCRIPTION_PREFIX = STYLE_PREFIX + "subscription-";
    private static final String STYLE_CLOSED = STYLE_SUBSCRIPTION_PREFIX + "closed";

    private final Feed feed;

    private final FaLabel disclosure;
    private final Badge newSize;
    private final Badge updSize;

    private final Flow content;

    private boolean open;

    private SubscriptionPanel(Subscription subscription, boolean open) {
      super(STYLE_SUBSCRIPTION_PREFIX + "panel");
      if (!open) {
        addStyleName(STYLE_CLOSED);
      }

      this.feed = subscription.getFeed();
      this.open = open;

      Flow header = new Flow(STYLE_SUBSCRIPTION_PREFIX + "header");

      this.disclosure = new FaLabel(open ? FontAwesome.CARET_DOWN : FontAwesome.CARET_RIGHT);
      disclosure.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "disclosure");

      disclosure.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleOpen();
        }
      });

      header.add(disclosure);

      Label feedLabel = new Label(subscription.getLabel());
      feedLabel.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "label");

      feedLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleOpen();
        }
      });

      header.add(feedLabel);

      this.newSize = new Badge(subscription.countNew(), STYLE_SUBSCRIPTION_PREFIX + "new-size");
      header.add(newSize);

      this.updSize = new Badge(subscription.countUpdated(), STYLE_SUBSCRIPTION_PREFIX + "upd-size");
      header.add(updSize);

      FaLabel filter = new FaLabel(FontAwesome.FILTER);
      filter.addStyleName(STYLE_SUBSCRIPTION_PREFIX + "filter");
      filter.setTitle(Localized.getConstants().actionFilter());

      filter.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onFilter(getFeed());
        }
      });

      header.add(filter);

      add(header);

      this.content = new Flow(STYLE_SUBSCRIPTION_PREFIX + "content");

      for (Headline headline : subscription.getHeadlines()) {
        HeadlinePanel headlinePanel = new HeadlinePanel(headline);
        content.add(headlinePanel);
      }

      add(content);
    }

    private HeadlinePanel findHeadlinePanel(long dataId) {
      for (Widget widget : content) {
        if (widget instanceof HeadlinePanel && ((HeadlinePanel) widget).getDataId() == dataId) {
          return (HeadlinePanel) widget;
        }
      }
      return null;
    }

    private Feed getFeed() {
      return feed;
    }

    private boolean hasHeadlines() {
      return !content.isEmpty();
    }

    private boolean isOpen() {
      return open;
    }

    private boolean removeHeadline(long dataId) {
      HeadlinePanel headlinePanel = findHeadlinePanel(dataId);

      if (headlinePanel == null) {
        return false;
      
      } else {
        boolean ok = content.remove(headlinePanel);

        if (ok) {
          if (headlinePanel.isNew()) {
            newSize.decrement();
          } else {
            updSize.decrement();
          }
        }
        
        return ok;
      }
    }

    private void setOpen(boolean open) {
      this.open = open;
    }

    private void toggleOpen() {
      setOpen(!isOpen());

      setStyleName(STYLE_CLOSED, !isOpen());
      disclosure.setChar(isOpen() ? FontAwesome.CARET_DOWN : FontAwesome.CARET_RIGHT);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(NewsAggregator.class);

  private static final String STYLE_PREFIX = "bee-News-";

  private static void readHeadline(HeadlinePanel headlinePanel) {
    Feed feed = headlinePanel.getFeed();
    if (feed != null) {
      RowEditor.openRow(feed.getHeadlineView(), headlinePanel.getDataId(), false, null);
    }
  }

  private final List<Subscription> subscriptions = Lists.newArrayList();

  private final NewsPanel newsPanel = new NewsPanel();

  private Badge sizeBadge;

  private EnumMap<Feed, Consumer<GridOptions>> registeredFilterHandlers =
      Maps.newEnumMap(Feed.class);
  private Map<String, Consumer<Long>> registeredAccessHandlers = Maps.newHashMap();

  NewsAggregator() {
  }

  public int countNews() {
    int count = 0;
    for (Subscription subscription : subscriptions) {
      count += subscription.size();
    }
    return count;
  }

  public IdentifiableWidget getNewsPanel() {
    return newsPanel;
  }

  public boolean hasNews() {
    for (Subscription subscription : subscriptions) {
      if (!subscription.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void loadSubscriptions(String serialized) {
    String[] arr = Codec.beeDeserializeCollection(serialized);

    if (ArrayUtils.isEmpty(arr)) {
      logger.severe("cannot deserialize subscriptions");

    } else {
      clear(false);

      for (String s : arr) {
        final Subscription subscription = Subscription.restore(s);
        subscriptions.add(subscription);

        if (!subscription.isEmpty()) {
          SubscriptionPanel subscriptionPanel = new SubscriptionPanel(subscription,
              newsPanel.isOpen());
          newsPanel.addSubscriptionPanel(subscriptionPanel);
        }
      }

      updateHeader();
      logger.info("subscriptions", subscriptions.size(), countNews());
    }
  }

  public void onAccess(String viewName, long rowId) {
    if (registeredAccessHandlers.containsKey(viewName)) {
      registeredAccessHandlers.get(viewName).accept(rowId);
    }

    String table = Data.getViewTable(viewName);

    if (NewsConstants.hasUsageTable(table)) {
      ParameterList parameters = BeeKeeper.getRpc().createParameters(Service.ACCESS);
      parameters.addQueryItem(Service.VAR_TABLE, table);
      parameters.addQueryItem(Service.VAR_ID, rowId);

      BeeKeeper.getRpc().makeRequest(parameters);
    }
    
    removeData(viewName, rowId);
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
  }

  @Override
  public void onDataChange(DataChangeEvent event) {
  }

  @Override
  public void onMultiDelete(MultiDeleteEvent event) {
    List<Subscription> filteredSubscriptions = filterSubscriptions(event.getViewName());
    if (!filteredSubscriptions.isEmpty()) {
      for (RowInfo rowInfo : event.getRows()) {
        removeHeadline(filteredSubscriptions, rowInfo.getId());
      }
    }
  }

  @Override
  public void onRowDelete(RowDeleteEvent event) {
    removeData(event.getViewName(), event.getRowId());
  }

  @Override
  public void onRowInsert(RowInsertEvent event) {
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
  }

  public void refresh() {
    newsPanel.startRefresh();

    BeeKeeper.getRpc().makeGetRequest(Service.GET_NEWS, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!response.hasErrors()) {
          if (response.hasResponse()) {
            loadSubscriptions(response.getResponseAsString());
          } else {
            clear(true);
          }
        }

        newsPanel.endRefresh();
      }
    });
  }

  public void registerAccessHandler(String viewName, Consumer<Long> handler) {
    Assert.notEmpty(viewName);
    Assert.notNull(handler);

    registeredAccessHandlers.put(viewName, handler);
  }

  public void registerFilterHandler(Feed feed, Consumer<GridOptions> handler) {
    Assert.notNull(feed);
    Assert.notNull(handler);
    
    registeredFilterHandlers.put(feed, handler);
  }

  private void clear(boolean clearSizeBadge) {
    if (!subscriptions.isEmpty()) {
      subscriptions.clear();
    }
    newsPanel.clearSubscriptions();

    if (clearSizeBadge && getSizeBadge() != null) {
      getSizeBadge().setValue(0);
    }
  }

  private void dismissHeadline(HeadlinePanel headlinePanel) {
    Feed feed = headlinePanel.getFeed();
    if (feed != null) {
      onAccess(feed.getHeadlineView(), headlinePanel.getDataId());
    }
  }

  private List<Subscription> filterSubscriptions(String viewName) {
    List<Subscription> result = Lists.newArrayList();

    for (Subscription subscription : subscriptions) {
      if (Data.sameTable(viewName, subscription.getFeed().getHeadlineView())) {
        result.add(subscription);
      }
    }

    return result;
  }

  private Subscription findSubscription(Feed feed) {
    for (Subscription subscription : subscriptions) {
      if (subscription.getFeed() == feed) {
        return subscription;
      }
    }
    return null;
  }

  private Badge getSizeBadge() {
    return sizeBadge;
  }

  private void onFilter(Feed feed) {
    Subscription subscription = findSubscription(feed);

    if (subscription != null && !subscription.isEmpty()) {
      Set<Long> idSet = subscription.getIdSet();

      String caption = BeeUtils.joinWords(Domain.NEWS.getCaption(), feed.getCaption());
      Filter filter = Filter.idIn(idSet);
      GridOptions gridOptions = GridOptions.forCaptionAndFilter(caption, filter);

      if (registeredFilterHandlers.containsKey(feed)) {
        registeredFilterHandlers.get(feed).accept(gridOptions);
      } else {
        GridFactory.openGrid(feed.getHeadlineView(), gridOptions);
      }
    }
  }

  private void removeData(String viewName, long rowId) {
    List<Subscription> filteredSubscriptions = filterSubscriptions(viewName);
    if (!filteredSubscriptions.isEmpty()) {
      removeHeadline(filteredSubscriptions, rowId);
    }
  }

  private void removeHeadline(List<Subscription> subs, long rowId) {
    boolean changed = false;

    for (Subscription subscription : subs) {
      if (subscription.contains(rowId)) {
        subscription.remove(rowId);
        newsPanel.removeHeadline(subscription.getFeed(), rowId);

        changed = true;
      }
    }

    if (changed) {
      updateHeader();
    }
  }

  private void setSizeBadge(Badge sizeBadge) {
    this.sizeBadge = sizeBadge;
  }

  private void updateHeader() {
    Flow header = BeeKeeper.getScreen().getDomainHeader(Domain.NEWS, null);
    if (header == null) {
      return;
    }

    int size = countNews();

    if (getSizeBadge() == null) {
      Badge badge = new Badge(size, STYLE_PREFIX + "size");

      header.add(badge);
      setSizeBadge(badge);

    } else {
      getSizeBadge().update(size);
    }
  }
}
