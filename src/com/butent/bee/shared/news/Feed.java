package com.butent.bee.shared.news;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants;
import com.butent.bee.shared.modules.documents.DocumentsConstants;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.tasks.TasksConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.SubModule;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public enum Feed implements HasLocalizedCaption {
  TASKS_ASSIGNED(Module.TASKS.getName(), TasksConstants.TBL_TASKS, TasksConstants.VIEW_TASKS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTasksAssigned();
    }
  },

  TASKS_DELEGATED(Module.TASKS.getName(), TasksConstants.TBL_TASKS, TasksConstants.VIEW_TASKS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTasksDelegated();
    }
  },

  TASKS_OBSERVED(Module.TASKS.getName(), TasksConstants.TBL_TASKS, TasksConstants.VIEW_TASKS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTasksObserved();
    }
  },

  TASKS_ALL(Module.TASKS.getName(), TasksConstants.TBL_TASKS, TasksConstants.VIEW_TASKS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTasksAll();
    }
  },

  COMPANIES_MY(Module.CLASSIFIERS.getName(SubModule.CONTACTS), CommonsConstants.TBL_COMPANY_USERS,
      CommonsConstants.VIEW_COMPANIES, CommonsConstants.COL_COMPANY_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedCompaniesMy();
    }
  },

  COMPANIES_ALL(Module.CLASSIFIERS.getName(SubModule.CONTACTS), CommonsConstants.TBL_COMPANIES,
      CommonsConstants.VIEW_COMPANIES, CommonsConstants.COL_COMPANY_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedCompaniesAll();
    }
  },

  PERSONS(Module.CLASSIFIERS.getName(SubModule.CONTACTS), CommonsConstants.TBL_PERSONS,
      CommonsConstants.VIEW_PERSONS,
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedPersons();
    }
  },

  GOODS(Module.TRADE.getName(), CommonsConstants.TBL_ITEMS, CommonsConstants.VIEW_ITEMS,
      Lists.newArrayList(CommonsConstants.COL_ITEM_NAME),
      Lists.newArrayList(CommonsConstants.COL_ITEM_ARTICLE)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedGoods();
    }
  },

  DOCUMENTS(Module.DOCUMENTS.getName(), DocumentsConstants.TBL_DOCUMENTS,
      DocumentsConstants.VIEW_DOCUMENTS, DocumentsConstants.COL_DOCUMENT_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedDocuments();
    }
  },

  APPOINTMENTS_MY(Module.CALENDAR.getName(), CalendarConstants.TBL_APPOINTMENT_ATTENDEES,
      CalendarConstants.VIEW_APPOINTMENTS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedAppointmentsMy();
    }
  },

  APPOINTMENTS_ALL(Module.CALENDAR.getName(), CalendarConstants.TBL_APPOINTMENTS,
      CalendarConstants.VIEW_APPOINTMENTS) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedAppointmentsAll();
    }
  },

  EC_CLIENTS_MY(Module.ECOMMERCE.getName(), EcConstants.TBL_CLIENTS, EcConstants.VIEW_CLIENTS,
      Lists.newArrayList(CommonsConstants.ALS_COMPANY_NAME),
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedEcClientsMy();
    }
  },

  EC_CLIENTS_ALL(Module.ECOMMERCE.getName(), EcConstants.TBL_CLIENTS, EcConstants.VIEW_CLIENTS,
      Lists.newArrayList(CommonsConstants.ALS_COMPANY_NAME),
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedEcClientsAll();
    }
  },

  EC_ORDERS_MY(Module.ECOMMERCE.getName(), EcConstants.TBL_ORDERS, EcConstants.VIEW_ORDERS,
      Lists.newArrayList(EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME),
      Lists.newArrayList(EcConstants.COL_ORDER_DATE, EcConstants.COL_ORDER_STATUS)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedEcOrdersMy();
    }
  },

  EC_ORDERS_ALL(Module.ECOMMERCE.getName(), EcConstants.TBL_ORDERS, EcConstants.VIEW_ORDERS,
      Lists.newArrayList(EcConstants.ALS_ORDER_CLIENT_COMPANY_NAME),
      Lists.newArrayList(EcConstants.COL_ORDER_DATE, EcConstants.COL_ORDER_STATUS)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedEcOrdersAll();
    }
  },

  EC_REGISTRATIONS(Module.ECOMMERCE.getName(), EcConstants.TBL_REGISTRATIONS,
      EcConstants.VIEW_REGISTRATIONS, Lists.newArrayList(EcConstants.COL_REGISTRATION_FIRST_NAME,
          EcConstants.COL_REGISTRATION_LAST_NAME),
      Lists.newArrayList(EcConstants.COL_REGISTRATION_COMPANY_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedEcRegistrations();
    }
  },

  ORDER_CARGO(Module.TRANSPORT.getName(), TransportConstants.TBL_ORDER_CARGO,
      TransportConstants.VIEW_ORDER_CARGO,
      Lists.newArrayList(TransportConstants.COL_CARGO_DESCRIPTION),
      Lists.newArrayList(TransportConstants.loadingColumnAlias(TransportConstants.COL_PLACE_DATE),
          TransportConstants.loadingColumnAlias(CommonsConstants.ALS_CITY_NAME),
          TransportConstants.loadingColumnAlias(CommonsConstants.ALS_COUNTRY_NAME),
          TransportConstants.unloadingColumnAlias(TransportConstants.COL_PLACE_DATE),
          TransportConstants.unloadingColumnAlias(CommonsConstants.ALS_CITY_NAME),
          TransportConstants.unloadingColumnAlias(CommonsConstants.ALS_COUNTRY_NAME))) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrCargo();
    }
  },

  TRANSPORTATION_ORDERS_MY(Module.TRANSPORT.getName(), TransportConstants.TBL_ORDERS,
      TransportConstants.VIEW_ORDERS,
      Lists.newArrayList(TransportConstants.COL_ORDER_DATE, TransportConstants.COL_ORDER_NO),
      Lists.newArrayList(TransportConstants.COL_CUSTOMER_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrOrdersMy();
    }
  },

  TRANSPORTATION_ORDERS_ALL(Module.TRANSPORT.getName(), TransportConstants.TBL_ORDERS,
      TransportConstants.VIEW_ORDERS,
      Lists.newArrayList(TransportConstants.COL_ORDER_DATE, TransportConstants.COL_ORDER_NO),
      Lists.newArrayList(TransportConstants.COL_CUSTOMER_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrOrdersAll();
    }
  },

  TRIPS(Module.TRANSPORT.getName(), TransportConstants.TBL_TRIPS, TransportConstants.VIEW_TRIPS,
      Lists.newArrayList(TransportConstants.COL_TRIP_DATE, TransportConstants.COL_TRIP_NO,
          TransportConstants.ALS_VEHICLE_NUMBER)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrTrips();
    }
  },

  CARGO_REQUESTS_MY(Module.TRANSPORT.getName(), TransportConstants.TBL_CARGO_REQUESTS,
      TransportConstants.VIEW_CARGO_REQUESTS,
      Lists.newArrayList(TransportConstants.ALS_REQUEST_CUSTOMER_FIRST_NAME,
          TransportConstants.ALS_REQUEST_CUSTOMER_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrRequestsMy();
    }
  },

  CARGO_REQUESTS_ALL(Module.TRANSPORT.getName(), TransportConstants.TBL_CARGO_REQUESTS,
      TransportConstants.VIEW_CARGO_REQUESTS,
      Lists.newArrayList(TransportConstants.ALS_REQUEST_CUSTOMER_FIRST_NAME,
          TransportConstants.ALS_REQUEST_CUSTOMER_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrRequestsAll();
    }
  },

  SHIPMENT_REQUESTS_MY(Module.TRANSPORT.getName(), TransportConstants.TBL_SHIPMENT_REQUESTS,
      TransportConstants.VIEW_SHIPMENT_REQUESTS, TransportConstants.COL_QUERY_CUSTOMER_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrRequestsUnregisteredMy();
    }
  },

  SHIPMENT_REQUESTS_ALL(Module.TRANSPORT.getName(), TransportConstants.TBL_SHIPMENT_REQUESTS,
      TransportConstants.VIEW_SHIPMENT_REQUESTS, TransportConstants.COL_QUERY_CUSTOMER_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrRequestsUnregisteredAll();
    }
  },

  TRANSPORT_REGISTRATIONS(Module.TRANSPORT.getName(), TransportConstants.TBL_REGISTRATIONS,
      TransportConstants.VIEW_REGISTRATIONS, TransportConstants.COL_REGISTRATION_COMPANY_NAME) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrRegistrations();
    }
  },

  VEHICLES(Module.TRANSPORT.getName(), TransportConstants.TBL_VEHICLES,
      TransportConstants.VIEW_VEHICLES,
      Lists.newArrayList(TransportConstants.COL_VEHICLE_NUMBER, TransportConstants.COL_TYPE_NAME),
      Lists.newArrayList(TransportConstants.COL_PARENT_MODEL_NAME,
          TransportConstants.COL_MODEL_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrVehicles();
    }
  },

  DRIVERS(Module.TRANSPORT.getName(), TransportConstants.TBL_DRIVERS,
      TransportConstants.VIEW_DRIVERS,
      Lists.newArrayList(CommonsConstants.COL_FIRST_NAME, CommonsConstants.COL_LAST_NAME)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.feedTrDrivers();
    }
  },

  DISCUSSIONS(Module.DISCUSSIONS.getName(), DiscussionsConstants.TBL_DISCUSSIONS,
      DiscussionsConstants.VIEW_DISCUSSIONS, Lists.newArrayList(DiscussionsConstants.COL_SUBJECT)) {
    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.discussions();
    }
  },

  ANNOUNCEMENTS(Module.DISCUSSIONS.getName(), DiscussionsConstants.TBL_DISCUSSIONS,
      DiscussionsConstants.VIEW_DISCUSSIONS, Lists.newArrayList(DiscussionsConstants.COL_SUBJECT)) {

    @Override
    public String getCaption(LocalizableConstants constants) {
      return constants.announcements();
    }

  };

  private static final String SEPARATOR = BeeConst.STRING_COMMA;
  private static final Splitter splitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();

  public static String join(Collection<Feed> feeds) {
    if (BeeUtils.isEmpty(feeds)) {
      return BeeConst.STRING_EMPTY;
    }

    Set<Integer> ordinals = Sets.newHashSet();
    for (Feed feed : feeds) {
      if (feed != null) {
        ordinals.add(feed.ordinal());
      }
    }

    return BeeUtils.join(SEPARATOR, ordinals);
  }

  public static List<Feed> split(String input) {
    List<Feed> feeds = Lists.newArrayList();
    if (BeeUtils.isEmpty(input)) {
      return feeds;
    }

    for (String s : splitter.split(input)) {
      Feed feed = EnumUtils.getEnumByIndex(Feed.class, s);
      if (feed != null) {
        feeds.add(feed);
      }
    }
    return feeds;
  }

  private final String module;
  private final String table;

  private final String headlineView;

  private final List<String> labelColumns;
  private final List<String> titleColumns;

  private Feed(String module, String table, String headlineView) {
    this(module, table, headlineView, BeeConst.EMPTY_IMMUTABLE_STRING_LIST,
        BeeConst.EMPTY_IMMUTABLE_STRING_LIST);
  }

  private Feed(String module, String table, String headlineView, List<String> labelColumns) {
    this(module, table, headlineView, labelColumns, BeeConst.EMPTY_IMMUTABLE_STRING_LIST);
  }

  private Feed(String module, String table, String headlineView, List<String> labelColumns,
      List<String> titleColumns) {
    this.module = module;
    this.table = table;
    this.headlineView = headlineView;
    this.labelColumns = labelColumns;
    this.titleColumns = titleColumns;
  }

  private Feed(String module, String table, String headlineView, String labelColumn) {
    this(module, table, headlineView, Lists.newArrayList(labelColumn),
        BeeConst.EMPTY_IMMUTABLE_STRING_LIST);
  }

  @Override
  public String getCaption() {
    return getCaption(Localized.getConstants());
  }

  public String getHeadlineView() {
    return headlineView;
  }

  public List<String> getLabelColumns() {
    return labelColumns;
  }

  public String getModule() {
    return module;
  }

  public String getTable() {
    return table;
  }

  public List<String> getTitleColumns() {
    return titleColumns;
  }

  public String getUsageTable() {
    return (table == null) ? null : NewsConstants.getUsageTable(table);
  }

  public boolean in(Feed... feeds) {
    if (feeds != null) {
      for (Feed feed : feeds) {
        if (feed == this) {
          return true;
        }
      }
    }
    return false;
  }
}
