package com.butent.bee.client.modules.projects;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class ProjectsGrid extends AllProjectsGrid {

  private static final Long USER_ID = BeeKeeper.getUser().getUserId();

  @Override
  public GridInterceptor getInstance() {
    return new ProjectsGrid();
  }

  @Override
  public boolean initDescription(GridDescription gridDescription) {
    gridDescription.setFilter(getGridFilter());
    return true;
  }

  private static Filter getGridFilter() {
    Filter isOwner = Filter.equals(COL_PROJECT_OWNER, USER_ID);
    Filter isProjectUser =
        Filter.in(Data.getIdColumn(VIEW_PROJECTS), VIEW_PROJECT_USERS, COL_PROJECT, Filter.equals(
            AdministrationConstants.COL_USER, USER_ID));
    return Filter.or(isOwner, isProjectUser);
  }

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {
    if (BeeUtils.same(columnName, NAME_MODE) && column instanceof HasCellRenderer) {
      ((HasCellRenderer) column).setRenderer(new ModeRenderer());
    }

    return true;
  }
}
