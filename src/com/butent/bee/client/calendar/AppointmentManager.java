package com.butent.bee.client.calendar;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AppointmentManager {

  private Appointment selectedAppointment = null;

  private Appointment hoveredAppointment = null;

  private Appointment rollbackAppointment = null;

  private Appointment committedAppointment = null;

  private final List<Appointment> appointments = Lists.newArrayList();

  private boolean sortPending = false;

  public AppointmentManager() {
    super();
  }

  public void addAppointment(Appointment appt) {
    if (appt != null) {
      appointments.add(appt);
      sortPending = true;
    }
  }

  public void addAppointments(Collection<Appointment> appts) {
    if (appts != null) {
      for (Appointment appointment : appts) {
        addAppointment(appointment);
      }
    }
  }

  public void clearAppointments() {
    appointments.clear();
  }

  public void commit() {
    rollbackAppointment = null;
    committedAppointment = null;
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }

  public Appointment getHoveredAppointment() {
    return hoveredAppointment;
  }

  public Appointment getSelectedAppointment() {
    return selectedAppointment;
  }

  public boolean hasAppointmentSelected() {
    return selectedAppointment != null;
  }

  public boolean isTheSelectedAppointment(Appointment appointment) {
    return hasAppointmentSelected() && selectedAppointment.equals(appointment);
  }

  public void removeAppointment(Appointment appointment) {
    if (appointment != null) {
      boolean wasRemoved = appointments.remove(appointment);
      if (wasRemoved) {
        sortPending = true;
      }

      if (hasAppointmentSelected() && getSelectedAppointment().equals(appointment)) {
        selectedAppointment = null;
      }
    }
  }

  public void removeCurrentlySelectedAppointment() {
    if (hasAppointmentSelected()) {
      removeAppointment(getSelectedAppointment());
      selectedAppointment = null;
    }
  }

  public void resetHoveredAppointment() {
    this.hoveredAppointment = null;
  }

  public void resetSelectedAppointment() {
    if (hasAppointmentSelected()) {
      selectedAppointment = null;
    }
  }

  public void rollback() {
    if (rollbackAppointment == null && committedAppointment == null) {
      return;
    }

    if (committedAppointment == null) {
      addAppointment(rollbackAppointment);
    } else if (rollbackAppointment == null) {
      removeAppointment(committedAppointment);
    } else {
      removeAppointment(committedAppointment);
      addAppointment(rollbackAppointment);
    }

    commit();
  }

  public boolean selectNextAppointment() {
    boolean moveSucceeded = false;

    if (getSelectedAppointment() != null) {
      int selectedApptIndex = getAppointments().indexOf(getSelectedAppointment());
      int lastIndex = getAppointments().size() - 1;

      if (selectedApptIndex < lastIndex) {
        Appointment nextAppt = getAppointments().get(selectedApptIndex + 1);
        if (nextAppt != null) {
          selectedAppointment = nextAppt;
          moveSucceeded = true;
        }
      }
    }
    return moveSucceeded;
  }

  public boolean selectPreviousAppointment() {
    boolean moveSucceeded = false;
 
    if (getSelectedAppointment() != null) {
      int selectedApptIndex = getAppointments().indexOf(getSelectedAppointment());
      if (selectedApptIndex > 0) {
        Appointment prevAppt = getAppointments().get(selectedApptIndex - 1);
        if (prevAppt != null) {
          selectedAppointment = prevAppt;
          moveSucceeded = true;
        }
      }
    }
    return moveSucceeded;
  }

  public void setCommittedAppointment(Appointment appt) {
    sortPending = true;
    committedAppointment = appt;
  }

  public void setHoveredAppointment(Appointment hoveredAppointment) {
    this.hoveredAppointment = hoveredAppointment;
  }

  public void setRollbackAppointment(Appointment appt) {
    sortPending = true;
    commit();
    rollbackAppointment = appt;
  }

  public void setSelectedAppointment(Appointment selectedAppointment) {
    if (selectedAppointment != null && appointments.contains(selectedAppointment)) {
      this.selectedAppointment = selectedAppointment;
    }
  }

  public void sortAppointments() {
    if (sortPending) {
      Collections.sort(appointments);
      sortPending = false;
    }
  }
}