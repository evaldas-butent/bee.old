package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;

import java.util.Collection;
import java.util.List;

public class CalendarKeeper {

  private static class RowActionHandler implements RowActionEvent.Handler {
    public void onRowAction(final RowActionEvent event) {
      if (event.hasView(VIEW_CALENDARS)) {
        Long calId = event.getRowId();
        if (DataUtils.isId(calId)) {
          String calName = event.hasRow() ?
              Data.getString(VIEW_CALENDARS, event.getRow(), COL_NAME) : event.getOptions();
          openCalendar(calId, calName);
        }
      }
    }
  }

  static final CalendarCache CACHE = new CalendarCache();
  
  private static final List<String> CACHED_VIEWS =
      Lists.newArrayList(VIEW_CONFIGURATION, VIEW_APPOINTMENT_TYPES, VIEW_ATTENDEES,
          VIEW_EXTENDED_PROPERTIES, VIEW_REMINDER_TYPES, VIEW_THEMES, VIEW_THEME_COLORS,
          VIEW_ATTENDEE_PROPS, VIEW_APPOINTMENT_STYLES);

  private static final AppointmentRenderer APPOINTMENT_RENDERER = new AppointmentRenderer();

  private static FormView settingsForm = null;

  public static String getAttendeeName(long id) {
    return CACHE.getString(VIEW_ATTENDEES, id, COL_NAME);
  }

  public static String getPropertyName(long id) {
    return CACHE.getString(VIEW_EXTENDED_PROPERTIES, id, COL_NAME);
  }

  public static String getReminderTypeName(long id) {
    return CACHE.getString(VIEW_REMINDER_TYPES, id, COL_NAME);
  }
  
  public static void register() {
    Global.registerCaptions(AppointmentStatus.class);
    Global.registerCaptions(ReminderMethod.class);
    Global.registerCaptions(ResponseStatus.class);
    Global.registerCaptions(Transparency.class);
    Global.registerCaptions("Calendar_Visibility", Visibility.class);

    Global.registerCaptions(TimeBlockClick.class);

    GridFactory.registerGridCallback(GRID_APPOINTMENTS, new AppointmentGridHandler());

    BeeKeeper.getBus().registerDataHandler(CACHE, true);
    BeeKeeper.getBus().registerRowActionHandler(new RowActionHandler(), false);

    SelectorEvent.register(new SelectorHandler());
  }

  static void createAppointment(final DateTime start, final Long attendeeId, final boolean glass) {
    final AppointmentBuilder builder = new AppointmentBuilder(true);

    FormFactory.createFormView(FORM_NEW_APPOINTMENT, VIEW_APPOINTMENTS,
        getAppointmentViewColumns(), false, builder, new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.start(null);
              result.updateRow(AppointmentBuilder.createEmptyRow(start), false);
              
              if (DataUtils.isId(attendeeId)) {
                builder.setAttenddes(Lists.newArrayList(attendeeId));
              }
              
              Global.inputWidget(getAppointmentViewInfo().getNewRowCaption(), result.asWidget(),
                  builder.getModalCallback(), glass, RowFactory.DIALOG_STYLE_NEW);
            }
          }
        });
  }

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CALENDAR_MODULE);
    params.addQueryItem(CALENDAR_METHOD, service);
    return params;
  }

  static void editSettings(long id, final CalendarPanel calendarPanel) {
    getUserCalendar(id, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (getSettingsForm() == null) {
          createSettingsForm(result, calendarPanel);
        } else {
          openSettingsForm(result, calendarPanel);
        }
      }
    });
  }

  static BeeRow getAppointmentTypeRow(Appointment appointment) {
    Long type = appointment.getType();
    if (type == null) {
      type = getDefaultAppointmentType();
    }

    BeeRowSet rowSet = CACHE.getRowSet(VIEW_APPOINTMENT_TYPES);
    
    BeeRow row = null;
    if (rowSet != null && !rowSet.isEmpty()) {
      if (type != null) {
        row = rowSet.getRowById(type);
      }
      if (row == null && rowSet.getNumberOfRows() == 1) {
        row = rowSet.getRow(0);
      }
    }
    return row;
  }
  
  static List<BeeColumn> getAppointmentViewColumns() {
    return CACHE.getAppointmentViewColumns();
  }

  static DataInfo getAppointmentViewInfo() {
    return CACHE.getAppointmentViewInfo();
  }

  static BeeRowSet getAttendeeProps() {
    return CACHE.getRowSet(VIEW_ATTENDEE_PROPS);
  }

  static BeeRowSet getAttendees() {
    return CACHE.getRowSet(VIEW_ATTENDEES);
  }
  
  static Long getDefaultAppointmentType() {
    BeeRowSet rowSet = CACHE.getRowSet(VIEW_CONFIGURATION);
    if (rowSet != null) {
      for (BeeRow row : rowSet.getRows()) {
        Long type = Data.getLong(VIEW_CONFIGURATION, row, COL_APPOINTMENT_TYPE);
        if (type != null) {
          return type;
        }
      }
    }
    return null;
  }

  static BeeRowSet getExtendedProperties() {
    return CACHE.getRowSet(VIEW_EXTENDED_PROPERTIES);
  }

  static BeeRowSet getReminderTypes() {
    return CACHE.getRowSet(VIEW_REMINDER_TYPES);
  }

  static BeeRowSet getThemeColors() {
    return CACHE.getRowSet(VIEW_THEME_COLORS);
  }

  static BeeRowSet getThemes() {
    return CACHE.getRowSet(VIEW_THEMES);
  }
  
  static boolean isAttendeeOpaque(long id) {
    BeeRow row = CACHE.getRow(VIEW_ATTENDEES, id);
    if (row == null) {
      return false;
    }
    
    Integer value = Data.getInteger(VIEW_ATTENDEES, row, COL_TRANSPARENCY);
    if (value != null) {
      return Transparency.isOpaque(value);
    } else {
      return Transparency.isOpaque(Data.getInteger(VIEW_ATTENDEES, row, COL_TYPE_TRANSPARENCY));
    }
  }

  static void loadData(Collection<String> viewNames, CalendarCache.MultiCallback multiCallback) {
    CACHE.getData(viewNames, multiCallback);
  }

  static void openAppointment(final Appointment appointment, final boolean glass) {
    Assert.notNull(appointment);
    final AppointmentBuilder builder = new AppointmentBuilder(false);

    FormFactory.createFormView(FORM_EDIT_APPOINTMENT, VIEW_APPOINTMENTS,
        getAppointmentViewColumns(), false, builder, new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.start(null);
              result.updateRow(appointment.getRow(), false);
              
              builder.setAttenddes(appointment.getAttendees());
              builder.setProperties(appointment.getProperties());
              builder.setReminders(appointment.getReminders());
              
              builder.setColor(appointment.getColor());

              Global.inputWidget(result.getCaption(), result.asWidget(),
                  builder.getModalCallback(), glass, RowFactory.DIALOG_STYLE_EDIT);
            }
          }
        });
  }
  
  static void renderAppoinment(AppointmentWidget appointmentWidget, boolean multi) {
    BeeRow row = getAppointmentTypeRow(appointmentWidget.getAppointment());
    if (row == null) {
      if (multi) {
        APPOINTMENT_RENDERER.renderMulti(appointmentWidget);
      } else {
        APPOINTMENT_RENDERER.renderSimple(appointmentWidget);
      }

    } else {
      String viewName = VIEW_APPOINTMENT_TYPES;
      String header = Data.getString(viewName, row, multi ? COL_MULTI_HEADER : COL_SIMPLE_HEADER);
      String body = Data.getString(viewName, row, multi ? COL_MULTI_BODY : COL_SIMPLE_BODY);
      String title = Data.getString(viewName, row, COL_APPOINTMENT_TITLE);
      
      APPOINTMENT_RENDERER.render(appointmentWidget, header, body, title, multi);
    }
    
    BeeRow styleRow = getAppointmentStyleRow(appointmentWidget.getAppointment(), row);
    if (styleRow != null) {
      String viewName = VIEW_APPOINTMENT_STYLES;
      String panel = Data.getString(viewName, styleRow, multi ? COL_MULTI : COL_SIMPLE);
      
      String header = Data.getString(viewName, styleRow, COL_HEADER);
      String body = Data.getString(viewName, styleRow, COL_BODY);
      String footer = Data.getString(viewName, styleRow, COL_FOOTER);
      
      CalendarStyleManager.applyStyle(appointmentWidget, panel, header, body, footer);
    }
  }

  static void renderCompact(AppointmentWidget panel, Widget htmlWidget, Widget titleWidget) {
    BeeRow row = getAppointmentTypeRow(panel.getAppointment());

    String compact;
    String title;

    if (row == null) {
      compact = null;
      title = null;
    } else {
      String viewName = VIEW_APPOINTMENT_TYPES;
      compact = Data.getString(viewName, row, COL_APPOINTMENT_COMPACT);
      title = Data.getString(viewName, row, COL_APPOINTMENT_TITLE);
    }

    APPOINTMENT_RENDERER.renderCompact(panel.getAppointment(), compact, htmlWidget, title,
        titleWidget);
    
    BeeRow styleRow = getAppointmentStyleRow(panel.getAppointment(), row);
    if (styleRow != null) {
      String styles = Data.getString(VIEW_APPOINTMENT_STYLES, styleRow, COL_COMPACT);
      CalendarStyleManager.applyStyle(panel, styles);
    }
  }
  
  static void saveActiveView(final CalendarSettings settings) {
    if (settings != null && settings.getActiveView() != null) {
      ParameterList params = createRequestParameters(SVC_SAVE_ACTIVE_VIEW);
      params.addQueryItem(PARAM_USER_CALENDAR_ID, settings.getId());
      params.addQueryItem(PARAM_ACTIVE_VIEW, settings.getActiveView().ordinal());

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        public void onResponse(ResponseObject response) {
          if (response.hasResponse(Integer.class)) {
            BeeKeeper.getLog().debug(SVC_SAVE_ACTIVE_VIEW, settings.getActiveView().name(),
                response.getResponse());
          }
        }
      });
    }
  }
  
  private static void createSettingsForm(final BeeRowSet rowSet, final CalendarPanel cp) {
    FormFactory.createFormView(FORM_CALENDAR_SETTINGS, null, rowSet.getColumns(), false,
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null && getSettingsForm() == null) {
              setSettingsForm(result);
              getSettingsForm().setEditing(true);
              getSettingsForm().start(null);
            }

            openSettingsForm(rowSet, cp);
          }
        });
  }

  private static BeeRow getAppointmentStyleRow(Appointment appointment, BeeRow typeRow) {
    Long style = appointment.getStyle();
    if (style == null && typeRow != null) {
      style = Data.getLong(VIEW_APPOINTMENT_TYPES, typeRow, COL_STYLE);
    }
    
    if (style == null) {
      return null;
    } else {
      return CACHE.getRow(VIEW_APPOINTMENT_STYLES, style);
    }
  }
  
  private static FormView getSettingsForm() {
    return settingsForm;
  }

  private static void getUserCalendar(long id, final Queries.RowSetCallback callback) {
    ParameterList params = createRequestParameters(SVC_GET_USER_CALENDAR);
    params.addQueryItem(PARAM_CALENDAR_ID, id);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore((String) response.getResponse()));
        }
      }
    });
  }

  private static void openCalendar(final long id, final String name) {
    loadData(CACHED_VIEWS, new CalendarCache.MultiCallback() {
      @Override
      public void onSuccess(Integer cnt) {
        getUserCalendar(id, new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            CalendarSettings settings =
                CalendarSettings.create(result.getRow(0), result.getColumns());
            BeeKeeper.getScreen().updateActivePanel(new CalendarPanel(id, name, settings));
          }
        });
      }
    });
  }

  private static void openSettingsForm(final BeeRowSet rowSet, final CalendarPanel cp) {
    if (getSettingsForm() == null || getSettingsForm().asWidget().isAttached()) {
      return;
    }

    final BeeRow oldRow = rowSet.getRow(0);
    final BeeRow newRow = DataUtils.cloneRow(oldRow);
    
    if (cp.getSettings().getActiveView() != null) {
      Data.setValue(VIEW_USER_CALENDARS, newRow, COL_ACTIVE_VIEW,
          cp.getSettings().getActiveView().ordinal());
    }

    getSettingsForm().updateRow(newRow, false);

    String caption = getSettingsForm().getCaption();

    Global.inputWidget(caption, getSettingsForm().asWidget(), new InputWidgetCallback() {
      public void onSuccess() {
        int updCount = Queries.update(VIEW_USER_CALENDARS, rowSet.getColumns(), oldRow, newRow,
            null);

        if (updCount > 0) {
          cp.updateSettings(newRow, rowSet.getColumns());
        }
      }
    });
  }

  private static void setSettingsForm(FormView settingsForm) {
    CalendarKeeper.settingsForm = settingsForm;
  }

  private CalendarKeeper() {
  }
}
