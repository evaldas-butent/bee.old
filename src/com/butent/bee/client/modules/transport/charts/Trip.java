package com.butent.bee.client.modules.transport.charts;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.DndTarget;
import com.butent.bee.client.timeboard.HasColorSource;
import com.butent.bee.client.timeboard.TimeBoardHelper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.TripStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.VehicleType;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class Trip extends Filterable implements HasColorSource, HasDateRange, HasItemName {

  private static final String VIEW_NAME = VIEW_TRIPS;

  private static final String tripNoLabel = Data.getColumnLabel(VIEW_NAME, COL_TRIP_NO);
  private static final String truckLabel = Data.getColumnLabel(VIEW_NAME, COL_VEHICLE);
  private static final String trailerLabel = Data.getColumnLabel(VIEW_NAME, COL_TRAILER);
  private static final String notesLabel = Data.getColumnLabel(VIEW_NAME, COL_TRIP_NOTES);

  private static final String driversLabel = Data.getViewCaption(VIEW_DRIVERS);
  private static final String cargosLabel = Data.getViewCaption(VIEW_CARGO_TRIPS);
  private static final String customersLabel = Data.getColumnLabel(VIEW_ORDERS, COL_CUSTOMER);
  private static final String managersLabel = Data.getColumnLabel(VIEW_ORDERS, COL_ORDER_MANAGER);

  private static final Set<String> acceptsDropTypes =
      ImmutableSet.of(DATA_TYPE_TRUCK, DATA_TYPE_TRAILER, DATA_TYPE_FREIGHT, DATA_TYPE_ORDER_CARGO,
          DATA_TYPE_DRIVER);

  static void createForCargo(final Vehicle truck, final HasShipmentInfo cargo, String cargoTitle,
      final RowCallback callback) {

    if (truck == null || BeeUtils.isEmpty(cargoTitle) || callback == null) {
      return;
    }

    List<String> messages = Lists.newArrayList(cargoTitle, truck.getMessage(truckLabel),
        Localized.dictionary().createTripForCargoQuestion());

    Global.confirm(Localized.dictionary().createTripForCargoCaption(), Icon.QUESTION, messages,
        () -> {
          DataInfo dataInfo = Data.getDataInfo(VIEW_NAME);
          BeeRow newRow = RowFactory.createEmptyRow(dataInfo, true);

          newRow.setValue(dataInfo.getColumnIndex(COL_VEHICLE), truck.getId());
          if (cargo != null) {
            JustDate start = BeeUtils.nvl(cargo.getLoadingDate(), cargo.getUnloadingDate());
            if (start != null && TimeUtils.isMore(start, TimeUtils.today())) {
              newRow.setValue(dataInfo.getColumnIndex(COL_TRIP_DATE), start.getDateTime());
            }
          }

          Queries.insert(VIEW_NAME, dataInfo.getColumns(), newRow, new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              callback.onSuccess(result);
            }
          });
        });
  }

  static String getVehicleLabel(VehicleType vehicleType) {
    switch (vehicleType) {
      case TRUCK:
        return truckLabel;
      case TRAILER:
        return trailerLabel;
    }
    return null;
  }

  static List<Range<JustDate>> getVoidRanges(Range<JustDate> range,
      Set<JustDate> eventDates, Collection<? extends OrderCargo> cargos) {

    List<Range<JustDate>> result = new ArrayList<>();
    int tripDays = TimeBoardHelper.getSize(range);

    Set<JustDate> usedDates = new HashSet<>();

    if (!BeeUtils.isEmpty(eventDates)) {
      if (eventDates.size() >= tripDays) {
        return result;
      }
      usedDates.addAll(eventDates);
    }

    if (!BeeUtils.isEmpty(cargos)) {
      for (OrderCargo cargo : cargos) {
        if (TimeBoardHelper.isActive(cargo, range)) {
          Range<JustDate> cargoRange = TimeBoardHelper.normalizedIntersection(cargo.getRange(),
              range);
          if (cargoRange == null) {
            continue;
          }

          int cargoDays = TimeBoardHelper.getSize(cargoRange);
          if (cargoDays >= tripDays) {
            return result;
          }

          for (int i = 0; i < cargoDays; i++) {
            usedDates.add(TimeUtils.nextDay(cargoRange.lowerEndpoint(), i));
          }

          if (usedDates.size() >= tripDays) {
            return result;
          }
        }
      }
    }

    if (BeeUtils.isEmpty(usedDates)) {
      result.add(range);
      return result;
    }

    List<JustDate> dates = new ArrayList<>(usedDates);
    Collections.sort(dates);

    for (int i = 0; i < dates.size(); i++) {
      JustDate date = dates.get(i);

      if (i == 0 && TimeUtils.isMore(date, range.lowerEndpoint())) {
        result.add(Range.closed(range.lowerEndpoint(), TimeUtils.previousDay(date)));
      }

      if (i > 0 && TimeUtils.dayDiff(dates.get(i - 1), date) > 1) {
        result.add(Range.closed(TimeUtils.nextDay(dates.get(i - 1)), TimeUtils.previousDay(date)));
      }

      if (i == dates.size() - 1 && TimeUtils.isLess(date, range.upperEndpoint())) {
        result.add(Range.closed(TimeUtils.nextDay(date), range.upperEndpoint()));
      }
    }

    return result;
  }

  static void maybeAssignCargo(String cargoMessage, String tripMessage,
      final ConfirmationCallback callback) {
    if (BeeUtils.isEmpty(cargoMessage) || BeeUtils.isEmpty(tripMessage) || callback == null) {
      return;
    }

    Global.confirm(Localized.dictionary().assignCargoToTripCaption(), Icon.QUESTION,
        Lists.newArrayList(cargoMessage, tripMessage,
            Localized.dictionary().assignCargoToTripQuestion()), callback);
  }

  private final Long tripId;
  private final Long tripVersion;
  private final String tripNo;

  private final TripStatus status;

  private final Long truckId;
  private final String truckNumber;
  private final Long trailerId;
  private final String trailerNumber;

  private final Range<JustDate> range;

  private final Collection<Driver> drivers;
  private final String title;

  private final String itemName;

  private final int cargoCount;

  private final Collection<String> customers;

  Trip(SimpleRow row, Collection<Driver> drivers) {
    this(row, drivers, null, null, 0,
        BeeConst.EMPTY_IMMUTABLE_STRING_SET, BeeConst.EMPTY_IMMUTABLE_STRING_SET);
  }

  Trip(SimpleRow row, Collection<Driver> drivers, JustDate minDate, JustDate maxDate,
      int cargoCount, Collection<String> customers, Collection<String> managers) {

    this.tripId = row.getLong(COL_TRIP_ID);
    this.tripVersion = row.getLong(ALS_TRIP_VERSION);
    this.tripNo = row.getValue(COL_TRIP_NO);

    this.status = EnumUtils.getEnumByIndex(TripStatus.class, row.getInt(COL_TRIP_STATUS));

    DateTime date = row.getDateTime(COL_TRIP_DATE);
    JustDate plannedEndDate = row.getDate(COL_TRIP_PLANNED_END_DATE);
    JustDate dateFrom = row.getDate(COL_TRIP_DATE_FROM);
    JustDate dateTo = row.getDate(COL_TRIP_DATE_TO);

    this.truckId = row.getLong(COL_VEHICLE);
    this.truckNumber = row.getValue(ALS_VEHICLE_NUMBER);
    this.trailerId = row.getLong(COL_TRAILER);
    this.trailerNumber = row.getValue(ALS_TRAILER_NUMBER);

    String notes = row.getValue(COL_TRIP_NOTES);

    JustDate start = BeeUtils.nvl(dateFrom, BeeUtils.min(date.getDate(), minDate));
    JustDate end = BeeUtils.nvl(dateTo, BeeUtils.max(plannedEndDate, maxDate));

    this.range = Range.closed(start, BeeUtils.max(start, end));

    this.drivers = drivers;

    String rangeLabel = TimeBoardHelper.getRangeLabel(this.range);

    this.title = TimeBoardHelper.buildTitle(
        Localized.dictionary().tripDuration(), rangeLabel,
        Localized.dictionary().status(), (this.status == null) ? null : this.status.getCaption(),
        tripNoLabel, this.tripNo,
        truckLabel, this.truckNumber,
        trailerLabel, this.trailerNumber,
        driversLabel, Driver.getNames(BeeConst.DEFAULT_LIST_SEPARATOR, drivers),
        cargosLabel, cargoCount,
        customersLabel, BeeUtils.joinItems(customers),
        managersLabel, BeeUtils.joinItems(managers),
        notesLabel, notes);

    this.itemName = BeeUtils.joinWords(rangeLabel, this.tripNo);

    this.cargoCount = cargoCount;
    this.customers = customers;
  }

  @Override
  public Long getColorSource() {
    return tripId;
  }

  @Override
  public String getItemName() {
    return itemName;
  }

  @Override
  public Range<JustDate> getRange() {
    return range;
  }

  String getCustomerNames() {
    return BeeUtils.joinItems(customers);
  }

  Collection<String> getCustomers() {
    return customers;
  }

  String getDriverNames() {
    return Driver.getNames(BeeConst.DEFAULT_LIST_SEPARATOR, drivers);
  }

  Collection<Driver> getDrivers() {
    return drivers;
  }

  TripStatus getStatus() {
    return status;
  }

  String getTitle() {
    return title;
  }

  Long getTrailerId() {
    return trailerId;
  }

  String getTrailerNumber() {
    return trailerNumber;
  }

  JustDate getTripArrival() {
    return range.upperEndpoint();
  }

  JustDate getTripDeparture() {
    return range.lowerEndpoint();
  }

  Long getTripId() {
    return tripId;
  }

  String getTripNo() {
    return tripNo;
  }

  Long getTripVersion() {
    return tripVersion;
  }

  Long getTruckId() {
    return truckId;
  }

  String getTruckNumber() {
    return truckNumber;
  }

  Long getVehicleId(VehicleType vehicleType) {
    switch (vehicleType) {
      case TRUCK:
        return getTruckId();
      case TRAILER:
        return getTrailerId();
    }
    return null;
  }

  String getVehicleNumber(VehicleType vehicleType) {
    switch (vehicleType) {
      case TRUCK:
        return truckNumber;
      case TRAILER:
        return trailerNumber;
    }
    return null;
  }

  boolean hasCargo() {
    return cargoCount > 0;
  }

  boolean hasDriver(Long driverId) {
    if (drivers != null) {
      for (Driver driver : drivers) {
        if (Objects.equals(driverId, driver.getId())) {
          return true;
        }
      }
    }
    return false;
  }

  boolean hasDrivers() {
    return !BeeUtils.isEmpty(drivers);
  }

  boolean isEditable() {
    return status != null && status.isEditable();
  }

  void makeTarget(final DndTarget widget, final String overStyle) {
    DndHelper.makeTarget(widget, acceptsDropTypes, overStyle,
        this::isTarget, (t, u) -> {
          widget.asWidget().removeStyleName(overStyle);
          acceptDrop(u);
        });
  }

  boolean matchesDrivers(ChartData driverData) {
    if (driverData == null) {
      return true;
    }

    if (hasDrivers()) {
      for (Driver driver : drivers) {
        if (driverData.contains(driver.getItemName())) {
          return true;
        }
      }
    }

    return false;
  }

  void maybeAddDriver(final Driver driver) {
    if (driver == null) {
      return;
    }

    final String viewName = VIEW_TRIP_DRIVERS;

    String driverTitle = TimeBoardHelper.join(Data.getColumnLabel(viewName, COL_DRIVER),
        driver.getItemName());

    Global.confirm(Localized.dictionary().assignDriverToTripCaption(), Icon.QUESTION,
        Lists.newArrayList(driverTitle, getTitle(),
            Localized.dictionary().assignDriverToTripQuestion()),
        () -> {
          List<BeeColumn> columns =
              Data.getColumns(viewName, Lists.newArrayList(COL_TRIP, COL_DRIVER));
          List<String> values = Queries.asList(getTripId(), driver.getId());

          Queries.insert(viewName, columns, values, null, new RowInsertCallback(viewName, null));
        });
  }

  void maybeUpdateVehicle(VehicleType vehicleType, Vehicle vehicle) {
    if (vehicleType == null || vehicle == null) {
      return;
    }

    final Long oldVehicleId = getVehicleId(vehicleType);
    final Long newVehicleId = vehicle.getId();

    if (Objects.equals(oldVehicleId, newVehicleId)) {
      return;
    }

    String caption;
    List<String> messages = Lists.newArrayList(vehicle.getMessage(getVehicleLabel(vehicleType)),
        getTitle());

    final List<BeeColumn> columns = new ArrayList<>();

    switch (vehicleType) {
      case TRUCK:
        caption = Localized.dictionary().assignTruckToTripCaption();
        messages.add(Localized.dictionary().assignTruckToTripQuestion());

        columns.add(Data.getColumn(VIEW_NAME, COL_VEHICLE));
        break;

      case TRAILER:
        caption = Localized.dictionary().assignTrailerToTripCaption();
        messages.add(Localized.dictionary().assignTrailerToTripQuestion());

        columns.add(Data.getColumn(VIEW_NAME, COL_TRAILER));
        break;

      default:
        Assert.untouchable();
        caption = null;
    }

    Global.confirm(caption, Icon.QUESTION, messages,
        () -> Queries.update(VIEW_NAME, getTripId(), getTripVersion(), columns,
            Queries.asList(oldVehicleId), Queries.asList(newVehicleId), null,
            new RowUpdateCallback(VIEW_NAME)));
  }

  private void acceptDrop(Object data) {
    if (DndHelper.isDataType(DATA_TYPE_TRUCK)) {
      maybeUpdateVehicle(VehicleType.TRUCK, (Vehicle) data);

    } else if (DndHelper.isDataType(DATA_TYPE_TRAILER)) {
      maybeUpdateVehicle(VehicleType.TRAILER, (Vehicle) data);

    } else if (DndHelper.isDataType(DATA_TYPE_FREIGHT)) {
      final Freight freight = (Freight) data;
      String freightTitle = freight.getTitle();

      Trip.maybeAssignCargo(freightTitle, getTitle(),
          () -> freight.updateTrip(getTripId(), RowCallback.refreshView(VIEW_CARGO_TRIPS)));

    } else if (DndHelper.isDataType(DATA_TYPE_ORDER_CARGO)) {
      final OrderCargo orderCargo = (OrderCargo) data;
      String cargoTitle = orderCargo.getTitle();

      Trip.maybeAssignCargo(cargoTitle, getTitle(),
          () -> orderCargo.assignToTrip(getTripId(), RowCallback.refreshView(VIEW_CARGO_TRIPS)));

    } else if (DndHelper.isDataType(DATA_TYPE_DRIVER)) {
      maybeAddDriver((Driver) data);
    }
  }

  private boolean isTarget(Object data) {
    if (DndHelper.isDataType(DATA_TYPE_TRUCK) && data instanceof Vehicle) {
      return !Objects.equals(getTruckId(), ((Vehicle) data).getId());

    } else if (DndHelper.isDataType(DATA_TYPE_TRAILER) && data instanceof Vehicle) {
      return !Objects.equals(getTrailerId(), ((Vehicle) data).getId());

    } else if (DndHelper.isDataType(DATA_TYPE_FREIGHT) && data instanceof Freight) {
      return !Objects.equals(getTripId(), ((Freight) data).getTripId());

    } else if (DndHelper.isDataType(DATA_TYPE_ORDER_CARGO) && data instanceof OrderCargo) {
      return true;

    } else if (DndHelper.isDataType(DATA_TYPE_DRIVER) && data instanceof Driver) {
      return !hasDriver(((Driver) data).getId());

    } else {
      return false;
    }
  }
}
