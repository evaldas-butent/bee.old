package com.butent.bee.client.view;

import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.shared.data.view.Filter;

public interface SearchView extends IsWidget {
  Filter getFilter();
}
