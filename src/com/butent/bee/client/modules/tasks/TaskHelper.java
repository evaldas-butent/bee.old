package com.butent.bee.client.modules.tasks;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.client.modules.mail.Relations.*;
import static com.butent.bee.shared.modules.tasks.TaskConstants.*;
import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.mail.Relations;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.tasks.TaskConstants;
import com.butent.bee.shared.modules.tasks.TaskUtils;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shared utility for Task creating or editing between create and edit task's forms.
 */
final class TaskHelper {

  static final String NAME_TASK_TREE = "TaskTree";
  static final String NAME_ORDER = "TaskEventsOrder";


  static ParameterList createTaskParams(FormView form, TaskConstants.TaskEvent event,
                                        BeeRow newRow, Collection<RowChildren> updatedRelations,
                                        String comment) {
    String viewName = form.getViewName();

    IsRow oldRow = form.getOldRow();

    BeeRowSet updated = DataUtils.getUpdated(viewName, form.getDataColumns(), oldRow, newRow,
        form.getChildrenForUpdate());

    if (!DataUtils.isEmpty(updated)) {
      BeeRow updRow = updated.getRow(0);

      for (int i = 0; i < updated.getNumberOfColumns(); i++) {
        int index = form.getDataIndex(updated.getColumnId(i));

        newRow.setValue(index, oldRow.getString(index));
        newRow.preliminaryUpdate(index, updRow.getString(i));
      }
    }

    if (readBoolean(NAME_ORDER)) {
      newRow.setProperty(PROP_DESCENDING, BeeConst.INT_TRUE);
    } else {
      newRow.removeProperty(PROP_DESCENDING);
    }

    BeeRowSet rowSet = new BeeRowSet(viewName, form.getDataColumns());
    rowSet.addRow(newRow);

    ParameterList params = TasksKeeper.createTaskRequestParameters(event);
    params.addDataItem(VAR_TASK_DATA, Codec.beeSerialize(rowSet));
    params.addDataItem(VAR_TASK_USERS, getTaskUsers(form, oldRow));

    if (!BeeUtils.isEmpty(comment)) {
      params.addDataItem(VAR_TASK_COMMENT, comment);
    }

    List<String> notes = TaskUtils.getUpdateNotes(Data.getDataInfo(viewName), oldRow, newRow);

    if (form.isEnabled()) {
      if (!TaskUtils.sameObservers(oldRow, newRow)) {
        String oldObservers = oldRow.getProperty(PROP_OBSERVERS);
        String newObservers = newRow.getProperty(PROP_OBSERVERS);

        MultiSelector selector = getMultiSelector(form, PROP_OBSERVERS);

        Set<Long> removed = DataUtils.getIdSetDifference(oldObservers, newObservers);
        for (long id : removed) {
          String label = selector.getRowLabel(id);
          if (!BeeUtils.isEmpty(label)) {
            notes.add(TaskUtils.getDeleteNote(Localized.dictionary().crmTaskObservers(), label));
          }
        }

        Set<Long> added = DataUtils.getIdSetDifference(newObservers, oldObservers);
        for (long id : added) {
          String label = selector.getRowLabel(id);
          if (!BeeUtils.isEmpty(label)) {
            notes.add(TaskUtils.getInsertNote(Localized.dictionary().crmTaskObservers(), label));
          }
        }
      }
    }

    if (!updatedRelations.isEmpty()
      && form.getWidgetByName(AdministrationConstants.TBL_RELATIONS) instanceof Relations) {

      params.addDataItem(VAR_TASK_RELATIONS, Codec.beeSerialize(updatedRelations));
      Relations relations = (Relations) form.getWidgetByName(AdministrationConstants.TBL_RELATIONS);
      Map<String, String> oldRelations = getOldRelations(relations);

      for (RowChildren relation : updatedRelations) {
        String caption = Data.getColumnLabel(relation.getRepository(), relation.getChildColumn());
        String relViewName = Data.getColumnRelation(relation.getRepository(),
          relation.getChildColumn());
        String oldValue = oldRelations.get(relViewName);
        String newValue = relation.getChildrenIds();

        Set<Long> removed = DataUtils.getIdSetDifference(oldValue, newValue);
        for (long id : removed) {
          String label = relations == null ? BeeUtils.toString(id)
            : relations.getSelectorRowLabel(relViewName, id);
          if (!BeeUtils.isEmpty(label)) {
            notes.add(TaskUtils.getDeleteNote(caption, label));
          }
        }

        Set<Long> added = DataUtils.getIdSetDifference(newValue, oldValue);
        for (long id : added) {
          String label = relations == null ? BeeUtils.toString(id)
            : relations.getSelectorRowLabel(relViewName, id);
          if (!BeeUtils.isEmpty(label)) {
            notes.add(TaskUtils.getInsertNote(caption, label));
          }
        }
      }
    }


    if (!notes.isEmpty()) {
      params.addDataItem(VAR_TASK_NOTES, Codec.beeSerialize(notes));
    }

    return params;
  }

  static Map<String, String> getOldRelations(Relations relations) {
    Map<String, String> oldRelations = new HashMap<>();

    if (relations == null) {
      return oldRelations;
    }

    relations.getOldRowChildren(true).forEach(relation -> {
      String relViewName = Data.getColumnRelation(relation.getRepository(),
        relation.getChildColumn());
      oldRelations.put(relViewName, relation.getChildrenIds());
    });

    return oldRelations;
  }

  static MultiSelector getMultiSelector(FormView form, String source) {
    Widget widget = form.getWidgetBySource(source);
    return (widget instanceof MultiSelector) ? (MultiSelector) widget : null;
  }

  static String getStorageKey(String name) {

    switch (name) {
      case NAME_TASK_TREE:
        return BeeUtils.join(BeeConst.STRING_MINUS, name, BeeKeeper.getUser().getUserId(),
            UiConstants.ATTR_SIZE);

      case NAME_ORDER:
        return BeeUtils.join(BeeConst.STRING_MINUS, name, BeeKeeper.getUser().getUserId());
    }
    return name;
  }

  static List<String> getRelatedViews() {
    List<String> views = new ArrayList<>();

    for (BeeColumn relColumn
        : Data.getDataInfo(AdministrationConstants.TBL_RELATIONS).getColumns()) {
      if (BeeUtils.isEmpty(
        Data.getColumnRelation(AdministrationConstants.TBL_RELATIONS, relColumn.getId()))) {
        continue;
      }

      views.add(Data.getColumnRelation(AdministrationConstants.TBL_RELATIONS, relColumn.getId()));
    }
    return views;
  }

  static String getTaskUsers(FormView form, IsRow row) {
    return DataUtils.buildIdList(TaskUtils.getTaskUsers(row, form.getDataColumns()));
  }

  static boolean readBoolean(String name) {
    String key = getStorageKey(name);
    return BeeKeeper.getStorage().hasItem(key);
  }


  private TaskHelper() {
  }
}