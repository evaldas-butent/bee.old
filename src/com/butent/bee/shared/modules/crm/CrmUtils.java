package com.butent.bee.shared.modules.crm;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.crm.CrmConstants.*;

import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.discussions.DiscussionsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;

import java.util.List;
import java.util.Set;

public final class CrmUtils {

  private static final BiMap<String, String> taskPropertyToRelation = HashBiMap.create();

  public static Set<String> getRelations() {
    return ensureTaskPropertyToRelation().inverse().keySet();
  }

  public static List<Long> getTaskUsers(IsRow row, List<BeeColumn> columns) {
    List<Long> users = Lists.newArrayList();

    Long owner = row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns));
    if (owner != null) {
      users.add(owner);
    }

    Long executor = row.getLong(DataUtils.getColumnIndex(COL_EXECUTOR, columns));
    if (executor != null && !users.contains(executor)) {
      users.add(executor);
    }

    List<Long> observers = DataUtils.parseIdList(row.getProperty(PROP_OBSERVERS));
    for (Long observer : observers) {
      if (!users.contains(observer)) {
        users.add(observer);
      }
    }
    return users;
  }

  public static boolean isScheduled(DateTime start) {
    return start != null && TimeUtils.dayDiff(TimeUtils.today(), start) > 0;
  }

  public static boolean sameObservers(IsRow oldRow, IsRow newRow) {
    if (oldRow == null || newRow == null) {
      return false;
    } else {
      return DataUtils.sameIdSet(oldRow.getProperty(PROP_OBSERVERS),
          newRow.getProperty(PROP_OBSERVERS));
    }
  }

  public static String translateRelationToTaskProperty(String relation) {
    return ensureTaskPropertyToRelation().inverse().get(relation);
  }

  public static String translateTaskPropertyToRelation(String propertyName) {
    return ensureTaskPropertyToRelation().get(propertyName);
  }

  private static BiMap<String, String> ensureTaskPropertyToRelation() {
    if (taskPropertyToRelation.isEmpty()) {
      taskPropertyToRelation.put(PROP_COMPANIES, COL_COMPANY);
      taskPropertyToRelation.put(PROP_PERSONS, COL_PERSON);
      taskPropertyToRelation.put(PROP_APPOINTMENTS, CalendarConstants.COL_APPOINTMENT);
      taskPropertyToRelation.put(PROP_DISCUSSIONS, DiscussionsConstants.COL_DISCUSSION);
      taskPropertyToRelation.put(PROP_TASKS, COL_TASK);
    }
    return taskPropertyToRelation;
  }

  private CrmUtils() {
  }
}
