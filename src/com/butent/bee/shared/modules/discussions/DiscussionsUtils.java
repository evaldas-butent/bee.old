package com.butent.bee.shared.modules.discussions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.discussions.DiscussionsConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.crm.CrmConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DiscussionsUtils {

  /*
   * public final class ParameterConsumer implements Consumer<Map<String, String>> {
   * 
   * @Override public void accept(Map<String, String> input) { // TODO Auto-generated method stub
   * 
   * }
   * 
   * }
   */

  private static final BiMap<String, String> discussionPropertyToRelation = HashBiMap.create();
  
  public static List<Long> getDiscussionMembers(IsRow row, List<BeeColumn> columns) {
    List<Long> users = Lists.newArrayList();

    Long owner = row.getLong(DataUtils.getColumnIndex(COL_OWNER, columns));
    if (owner != null) {
      users.add(owner);
    }

    List<Long> members = DataUtils.parseIdList(row.getProperty(PROP_MEMBERS));

    for (Long member : members) {
      if (!users.contains(member)) {
        users.add(member);
      }
    }

    return users;
  }

  public static void getDiscussionsParameters(final Consumer<Map<String, String>> params) {
    if (params == null) {
      return;
    }
    
    final Map<String, String> holder = Maps.newHashMap();

    for (final String parameterName : LIST_OF_PARAMETERS) {
      Global.getParameter(DISCUSSIONS_MODULE, parameterName, new Consumer<String>() {

        @Override
        public void accept(String input) {
          holder.put(parameterName, input);

          if (holder.size() == LIST_OF_PARAMETERS.length) {
            params.accept(holder);
          }
        }

      });
    }
  }
  
  public static Set<String> getRelations() {
    return ensureDiscussionPropertyToRelation().inverse().keySet();
  }

  /*public static boolean isDiscussionAdmin(String loginName) {
    if (BeeUtils.isEmpty(loginName)) {
      return false;
    }

    Boolean result = Boolean.FALSE;

    Global.getParameter(DISCUSSIONS_MODULE, PRM_DISCUSS_ADMIN, new Consumer<String>() {

      @Override
      public void accept(String input) {
        LogUtils.getRootLogger().debug("VALUE", input);

        if (!BeeUtils.isEmpty(input)) {
          result = true;
        }
      }
    });

    return result.booleanValue();
  }*/

  public static boolean sameMembers(IsRow oldRow, IsRow newRow) {
    if (oldRow == null || newRow == null) {
      return false;
    } else {
      return DataUtils
          .sameIdSet(oldRow.getProperty(PROP_MEMBERS), newRow.getProperty(PROP_MEMBERS));
    }
  }

  public static String translateDiscussionPropertyToRelation(String propertyName) {
    return ensureDiscussionPropertyToRelation().get(propertyName);
  }

  public static String translateRelationToDiscussionProperty(String relation) {
    return ensureDiscussionPropertyToRelation().inverse().get(relation);
  }

  private static BiMap<String, String> ensureDiscussionPropertyToRelation() {
    if (discussionPropertyToRelation.isEmpty()) {
      discussionPropertyToRelation.put(PROP_COMPANIES, CommonsConstants.COL_COMPANY);
      discussionPropertyToRelation.put(PROP_PERSONS, CommonsConstants.COL_PERSON);
      discussionPropertyToRelation.put(PROP_APPOINTMENTS, CalendarConstants.COL_APPOINTMENT);
      discussionPropertyToRelation.put(PROP_TASKS, CrmConstants.COL_TASK);
      discussionPropertyToRelation.put(PROP_DOCUMENTS, CrmConstants.COL_DOCUMENT);
    }

    return discussionPropertyToRelation;
  }

  private DiscussionsUtils() {
  }
}
