package com.butent.bee.client.modules.trade;

import com.google.gwt.dom.client.Element;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.modules.trade.DebtKind;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class TradeDebtsGrid extends AbstractGridInterceptor {

  private static final String STYLE_PHASE_TOGGLE = TradeKeeper.STYLE_PREFIX + "debts-phase-toggle";

  private static final String PARENT_FILTER_KEY = "parent";
  private static final String PHASE_FILTER_KEY = "phase";

  private final DebtKind debtKind;

  private EnumSet<TradeDocumentPhase> phases = EnumSet.noneOf(TradeDocumentPhase.class);

  TradeDebtsGrid(DebtKind debtKind) {
    this.debtKind = debtKind;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    if (presenter != null && presenter.getHeader() != null) {
      initPhases();

      HeaderView header = presenter.getHeader();
      header.clearCommandPanel();

      for (TradeDocumentPhase phase : TradeDocumentPhase.values()) {
        CheckBox checkBox = new CheckBox(phase.getCaption());
        checkBox.setChecked(phases.contains(phase));
        checkBox.addStyleName(STYLE_PHASE_TOGGLE);

        DomUtils.setDataIndex(checkBox.getElement(), phase.ordinal());

        checkBox.addClickHandler(event -> {
          Element target = EventUtils.getEventTargetElement(event);
          Integer index = DomUtils.getDataIndexInt(DomUtils.getParentByClassName(target,
              STYLE_PHASE_TOGGLE, true));

          TradeDocumentPhase ph = EnumUtils.getEnumByIndex(TradeDocumentPhase.class, index);

          if (ph != null) {
            if (phases.contains(ph)) {
              phases.remove(ph);
            } else {
              phases.add(ph);
            }

            onPhaseChange();
          }
        });

        header.addCommandItem(checkBox);
      }
    }

    super.afterCreatePresenter(presenter);
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeDebtsGrid(debtKind);
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    Map<String, Filter> filters = new HashMap<>();
    filters.put(PARENT_FILTER_KEY, Filter.isFalse());

    return filters;
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();

    IsRow row = event.getRowValue();
    if (row != null) {
      RowEditor.open(getViewName(), row, Opener.NEW_TAB);
    }
  }

  void onParentChange(Long company, Long currency, DateTime dateTo, DateTime termTo) {
    Filter filter = buildParentFilter(company, currency, dateTo, termTo);
    maybeRefresh(PARENT_FILTER_KEY, filter);
  }

  private void onPhaseChange() {
    maybeRefresh(PHASE_FILTER_KEY, buildPhaseFilter());
  }

  private Filter buildParentFilter(Long company, Long currency, DateTime dateTo, DateTime termTo) {
    if (DataUtils.isId(company)) {
      List<String> args = new ArrayList<>();
      args.add(Codec.pack(debtKind));

      args.add(BeeUtils.toStringOrNull(company));
      args.add(BeeUtils.toStringOrNull(currency));

      args.add((dateTo == null) ? null : BeeUtils.toString(dateTo.getTime()));
      args.add((termTo == null) ? null : BeeUtils.toString(termTo.getTime()));

      return Filter.custom(FILTER_HAS_TRADE_DEBT, args);

    } else {
      return Filter.isFalse();
    }
  }

  private Filter buildPhaseFilter() {
    if (phases.isEmpty() || phases.containsAll(EnumSet.allOf(TradeDocumentPhase.class))) {
      return null;
    } else {
      return Filter.any(COL_TRADE_DOCUMENT_PHASE, phases);
    }
  }

  private void initPhases() {
    phases.clear();
    phases.addAll(Arrays.stream(TradeDocumentPhase.values())
        .filter(TradeDocumentPhase::modifyStock)
        .collect(Collectors.toSet()));
  }

  private void maybeRefresh(String key, Filter filter) {
    GridPresenter presenter = getGridPresenter();
    if (presenter != null && presenter.getDataProvider().setParentFilter(key, filter)) {
      presenter.handleAction(Action.REFRESH);
    }
  }
}