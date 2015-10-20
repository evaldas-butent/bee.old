package com.butent.bee.client.modules.mail;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.utils.Codec;

import java.util.HashSet;
import java.util.Set;

public class ContactsCreator extends AbstractGridInterceptor implements ClickHandler {

  private Long targetId;
  private boolean isNewsletter;

  public ContactsCreator(Long targetId, boolean isNewsletter) {
    this.targetId = targetId;
    this.isNewsletter = isNewsletter;
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().addCommandItem(new Button(Localized.getConstants().mailAddContacts(),
        this));
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Set<Long> ids = new HashSet<>();

    for (RowInfo row : getGridView().getSelectedRows(GridView.SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    String colName = "";

    switch (getGridView().getViewName()) {
      case VIEW_SELECT_COMPANIES:
        colName = COL_COMPANY;
        break;

      case VIEW_SELECT_COMPANY_PERSONS:
        colName = COL_COMPANY_PERSON;
        break;

      case VIEW_SELECT_COMPANY_CONTACTS:
        colName = COL_COMPANY_CONTACT;
        break;

      default:
        return;
    }

    if (DataUtils.isId(targetId) && !isNewsletter) {
      BeeRowSet rowSet =
          new BeeRowSet(VIEW_RCPS_GROUPS_CONTACTS, Data.getColumns(VIEW_RCPS_GROUPS_CONTACTS));
      int indexId = Data.getColumnIndex(MailConstants.VIEW_RCPS_GROUPS_CONTACTS,
          COL_RECIPIENTS_GROUP);
      int index = Data.getColumnIndex(MailConstants.VIEW_RCPS_GROUPS_CONTACTS, colName);

      for (Long id : ids) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());
        row.setValue(indexId, targetId);
        row.setValue(index, id);
        rowSet.addRow(row);
      }
      Queries.insertRows(rowSet);

    } else if (ids.size() > 0 && DataUtils.isId(targetId)) {
      ParameterList params = MailKeeper.createArgs(SVC_GET_NEWSLETTER_CONTACTS);
      params.addDataItem(Service.VAR_DATA, Codec.beeSerialize(ids));
      params.addDataItem(Service.VAR_COLUMN, colName);
      params.addDataItem(COL_NEWSLETTER, targetId);

      BeeKeeper.getRpc().makeRequest(params);
    }

    Popup.getActivePopup().close();
  }
}
