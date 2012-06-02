package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class Appointment implements Comparable<Appointment> {

  private final IsRow row;
  
  private final List<Long> attendees = Lists.newArrayList();
  private final List<Long> properties = Lists.newArrayList();
  private final List<Long> reminders = Lists.newArrayList();

  public Appointment(IsRow row) {
    this.row = row;
  }
  
  public Appointment(IsRow row, String attList, String propList, String remindList) {
    this(row);
    
    if (!BeeUtils.isEmpty(attList)) {
      attendees.addAll(DataUtils.parseList(attList));
    }
    if (!BeeUtils.isEmpty(propList)) {
      properties.addAll(DataUtils.parseList(propList));
    }
    if (!BeeUtils.isEmpty(remindList)) {
      reminders.addAll(DataUtils.parseList(remindList));
    }
  }
  
  public Appointment clone() {
    Appointment clone = new Appointment(DataUtils.cloneRow(row));

    if (!getAttendees().isEmpty()) {
      clone.getAttendees().addAll(getAttendees());
    }
    if (!getProperties().isEmpty()) {
      clone.getProperties().addAll(getProperties());
    }
    if (!getReminders().isEmpty()) {
      clone.getReminders().addAll(getReminders());
    }

    return clone;
  }

  public int compareTo(Appointment appointment) {
    int compare = BeeUtils.compare(getStart(), appointment.getStart());
    if (compare == BeeConst.COMPARE_EQUAL) {
      compare = BeeUtils.compare(appointment.getEnd(), getEnd());
    }
    return compare;
  }
  
  public List<Long> getAttendees() {
    return attendees;
  }

  public String getBackground() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_BACKGROUND);
  }

  public Long getColor() {
    return Data.getLong(VIEW_APPOINTMENTS, row, COL_COLOR);
  }
  
  public String getCompanyName() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_COMPANY_NAME);
  }
  
  public String getDescription() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_DESCRIPTION);
  }

  public DateTime getEnd() {
    return Data.getDateTime(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME);
  }

  public String getForeground() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_FOREGROUND);
  }
  
  public long getId() {
    return row.getId();
  }

  public List<Long> getProperties() {
    return properties;
  }

  public List<Long> getReminders() {
    return reminders;
  }

  public IsRow getRow() {
    return row;
  }

  public DateTime getStart() {
    return Data.getDateTime(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME);
  }

  public String getSummary() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_SUMMARY);
  }

  public String getVehicleModel() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_VEHICLE_MODEL);
  }

  public String getVehicleNumber() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_VEHICLE_NUMBER);
  }

  public String getVehicleParentModel() {
    return Data.getString(VIEW_APPOINTMENTS, row, COL_VEHICLE_PARENT_MODEL);
  }
  
  public boolean isAllDay() {
    return isMultiDay();
  }

  public boolean isMultiDay() {
    if (getStart() != null) {
      return !TimeUtils.sameDate(getStart(), getEnd());
    } else {
      return false;
    }
  }

  public void setEnd(DateTime end) {
    Data.setValue(VIEW_APPOINTMENTS, row, COL_END_DATE_TIME, end);
  }

  public void setStart(DateTime start) {
    Data.setValue(VIEW_APPOINTMENTS, row, COL_START_DATE_TIME, start);
  }
}
