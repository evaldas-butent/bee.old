package com.butent.bee.client.ui;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Rulers;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.style.Font;
import com.butent.bee.client.style.HasTextAlign;
import com.butent.bee.client.style.HasVerticalAlign;
import com.butent.bee.client.style.HasWhiteSpace;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.HasGridView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasBounds;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.css.CssProperties;
import com.butent.bee.shared.css.values.TextAlign;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.css.values.WhiteSpace;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Color;
import com.butent.bee.shared.ui.HasMaxLength;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

/**
 * Contains utility user interface creation functions like setting and getting horizontal alignment.
 */

public final class UiHelper {

  private static final BeeLogger logger = LogUtils.getLogger(UiHelper.class);

  public static void add(HasWidgets container, Holder<Widget> holder,
      WidgetInitializer initializer, String name) {
    Assert.notNull(holder);
    holder.set(add(container, holder.get(), initializer, name));
  }

  public static Widget add(HasWidgets container, Widget widget, WidgetInitializer initializer,
      String name) {
    if (container == null || widget == null) {
      return null;
    }

    if (initializer == null) {
      container.add(widget);
      return widget;
    }

    Widget w = initializer.initialize(widget, name);
    if (w != null) {
      container.add(w);
    }
    return w;
  }

  public static boolean closeDialog(Widget source) {
    if (source != null) {
      Popup popup = getParentPopup(source);
      if (popup != null) {
        popup.close();
        return true;
      }
    }
    return false;
  }

  public static boolean focus(Widget target) {
    if (DomUtils.focus(target)) {
      return true;

    } else if (target instanceof HasOneWidget) {
      return focus(((HasOneWidget) target).getWidget());

    } else if (target instanceof HasWidgets) {
      for (Widget child : (HasWidgets) target) {
        if (focus(child)) {
          return true;
        }
      }
      return false;

    } else {
      return false;
    }
  }

  public static Collection<Widget> getChildrenByStyleName(Widget parent,
      Collection<String> styleNames) {

    Collection<Widget> result = Lists.newArrayList();
    if (parent == null || styleNames == null) {
      return result;
    }
    
    if (StyleUtils.hasAnyClass(parent.getElement(), styleNames)) {
      result.add(parent);
    }

    if (parent instanceof HasOneWidget) {
      result.addAll(getChildrenByStyleName(((HasOneWidget) parent).getWidget(), styleNames));

    } else if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        result.addAll(getChildrenByStyleName(child, styleNames));
      }
    }

    return result;
  }
  
  public static DataView getDataView(Widget widget) {
    if (widget == null) {
      return null;
    }

    Widget p = widget;
    for (int i = 0; i < DomUtils.MAX_GENERATIONS; i++) {
      if (p instanceof DataView) {
        return (DataView) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return null;
  }

  public static TextAlign getDefaultHorizontalAlignment(ValueType type) {
    if (type == null) {
      return null;
    }

    TextAlign align;
    switch (type) {
      case BOOLEAN:
        align = TextAlign.CENTER;
        break;
      case DECIMAL:
      case INTEGER:
      case LONG:
      case NUMBER:
        align = TextAlign.END;
        break;
      default:
        align = null;
    }
    return align;
  }

  public static List<Focusable> getFocusableChildren(Widget parent) {
    List<Focusable> result = Lists.newArrayList();
    if (parent == null) {
      return result;
    }

    if (parent instanceof HasOneWidget) {
      result.addAll(getFocusableChildren(((HasOneWidget) parent).getWidget()));

    } else if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        result.addAll(getFocusableChildren(child));
      }

    } else if (parent instanceof Focusable) {
      if (DomUtils.isVisible(parent)) {
        result.add((Focusable) parent);
      }
    }
    return result;
  }

  public static FormView getForm(Widget widget) {
    if (widget == null) {
      return null;
    }

    Widget p = widget;
    for (int i = 0; i < DomUtils.MAX_GENERATIONS; i++) {
      if (p instanceof FormView) {
        return (FormView) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return null;
  }

  public static GridView getGrid(Widget widget) {
    DataView dataView = getDataView(widget);

    if (dataView == null) {
      return null;
    } else if (dataView instanceof GridView) {
      return (GridView) dataView;
    } else if (dataView.getViewPresenter() instanceof HasGridView) {
      return ((HasGridView) dataView.getViewPresenter()).getGridView();
    } else {
      return null;
    }
  }

  public static List<Widget> getImmediateChildren(Widget parent) {
    List<Widget> result = Lists.newArrayList();
    if (parent == null) {
      return result;
    }

    if (parent instanceof HasOneWidget) {
      Widget child = ((HasOneWidget) parent).getWidget();
      if (child != null) {
        result.add(child);
      }

    } else if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        if (child != null) {
          result.add(child);
        }
      }
    }
    return result;
  }

  public static int getMaxLength(IsColumn column) {
    if (column == null) {
      return BeeConst.UNDEF;
    }

    ValueType type = column.getType();
    int precision = column.getPrecision();
    int scale = Math.max(column.getScale(), 0);

    if (precision <= 0) {
      switch (type) {
        case BOOLEAN:
          precision = 1;
          break;

        case DATE:
          precision = 10;
          break;

        case DATE_TIME:
          precision = 23;
          break;

        case INTEGER:
          precision = Integer.toString(Integer.MAX_VALUE).length();
          break;

        case LONG:
          precision = Long.toString(Long.MAX_VALUE).length();
          break;

        case NUMBER:
          precision = 20;
          break;

        case TIME_OF_DAY:
          precision = 8;
          break;

        case DECIMAL:
        case TEXT:
      }
    }

    if (precision <= 0) {
      return BeeConst.UNDEF;
    } else if (ValueType.isNumeric(type)) {
      return precision + (precision - scale) / 3 + ((scale > 0) ? 2 : 1);
    } else {
      return precision;
    }
  }

  public static Popup getParentPopup(Widget w) {
    Assert.notNull(w);

    Widget p = w;
    for (int i = 0; i < DomUtils.MAX_GENERATIONS; i++) {
      if (p instanceof Popup) {
        return (Popup) p;
      }

      p = p.getParent();
      if (p == null) {
        break;
      }
    }
    return null;
  }

  public static Consumer<InputText> getTextBoxResizer(final int reserve) {
    return new Consumer<InputText>() {
      @Override
      public void accept(InputText input) {
        String value = input.getValue();

        int oldWidth = input.getOffsetWidth();
        int newWidth = reserve;

        if (value != null && value.length() > 0) {
          if (value.contains(BeeConst.STRING_SPACE)) {
            value = value.replace(BeeConst.STRING_SPACE, BeeConst.HTML_NBSP);
          }
          Font font = Font.getComputed(input.getElement());
          newWidth += Rulers.getAreaWidth(font, value, true);
        }

        if (newWidth != oldWidth) {
          StyleUtils.setWidth(input, newWidth);
        }
      }
    };
  }

  public static boolean hasImmediateChild(HasWidgets parent, String id) {
    if (parent == null || BeeUtils.isEmpty(id)) {
      return false;
    }

    for (Widget child : parent) {
      if (DomUtils.idEquals(child, id)) {
        return true;
      }
    }
    return false;
  }

  public static Widget initialize(Widget widget, WidgetInitializer initializer, String name) {
    if (widget == null) {
      return null;
    }
    if (initializer == null) {
      return widget;
    }
    return initializer.initialize(widget, name);
  }

  public static boolean isModal(Widget widget) {
    return getParentPopup(widget) != null;
  }

  public static boolean isSave(NativeEvent event) {
    if (event == null) {
      return false;
    }
    return EventUtils.isKeyDown(event.getType()) && event.getKeyCode() == KeyCodes.KEY_ENTER
        && EventUtils.hasModifierKey(event);
  }

  public static boolean maybeResize(Widget root, String id) {
    Widget child = DomUtils.getChildQuietly(root, id);
    if (child instanceof RequiresResize && child.isVisible()) {
      ((RequiresResize) child).onResize();
      return true;
    } else {
      return false;
    }
  }

  public static void maybeSetTitle(Widget widget, String title) {
    if (widget != null && !BeeUtils.isEmpty(title)) {
      widget.setTitle(title);
    }
  }

  public static boolean moveFocus(Widget parent, boolean forward) {
    return moveFocus(parent, DomUtils.getActiveElement(), forward);
  }

  public static boolean moveFocus(Widget parent, Element activeElement, boolean forward) {
    if (parent == null) {
      return false;
    }

    List<Focusable> children = getFocusableChildren(parent);
    if (children.isEmpty()) {
      return false;
    }
    int count = children.size();

    int index;

    if (activeElement == null) {
      index = forward ? 0 : count - 1;

    } else if (count == 1) {
      index = 0;
      if (isOrHasChild(children.get(index), activeElement)) {
        return false;
      }

    } else {
      index = BeeConst.UNDEF;
      for (int i = 0; i < count; i++) {
        if (isOrHasChild(children.get(i), activeElement)) {
          index = i;
          break;
        }
      }

      if (forward) {
        index = BeeUtils.rotateForwardExclusive(index, 0, count);
      } else {
        index = BeeUtils.rotateBackwardExclusive(index, 0, count);
      }
    }

    children.get(index).setFocus(true);
    return true;
  }

  public static void pressKey(ValueBoxBase<?> widget, char key) {
    Assert.notNull(widget);

    String oldText = BeeUtils.nvl(widget.getText(), BeeConst.STRING_EMPTY);

    int pos = widget.getCursorPos();
    int len = widget.getSelectionLength();

    if (len <= 0 && widget instanceof HasMaxLength) {
      int maxLength = ((HasMaxLength) widget).getMaxLength();
      if (maxLength > 0 && BeeUtils.hasLength(oldText, maxLength)) {
        return;
      }
    }

    String newText;
    if (len > 0) {
      newText = BeeUtils.replace(oldText, pos, pos + len, key);
    } else {
      newText = BeeUtils.insert(oldText, pos, key);
    }

    widget.setText(newText);
    widget.setCursorPos(pos + 1);
  }

  public static void selectDeferred(final ValueBoxBase<?> widget) {
    Assert.notNull(widget);
    final String text = widget.getText();
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (text.equals(widget.getText())) {
          widget.selectAll();
        }
      }
    });
  }

  public static void setBounds(HasBounds obj, String min, String max) {
    Assert.notNull(obj);
    if (!BeeUtils.isEmpty(min)) {
      obj.setMinValue(min);
    }
    if (!BeeUtils.isEmpty(max)) {
      obj.setMaxValue(max);
    }
  }

  public static void setColor(UIObject obj, Color color) {
    Assert.notNull(obj);
    Assert.notNull(color);

    if (!BeeUtils.isEmpty(color.getBackground())) {
      StyleUtils.setBackgroundColor(obj, color.getBackground());
    }
    if (!BeeUtils.isEmpty(color.getForeground())) {
      StyleUtils.setColor(obj, color.getForeground());
    }
  }

  public static void setDefaultBounds(HasBounds obj, IsColumn column) {
    Assert.notNull(obj);
    Assert.notNull(column);

    if (BeeUtils.isEmpty(obj.getMinValue())) {
      String min = DataUtils.getMinValue(column);
      if (!BeeUtils.isEmpty(min)) {
        obj.setMinValue(min);
      }
    }

    if (BeeUtils.isEmpty(obj.getMaxValue())) {
      String max = DataUtils.getMaxValue(column);
      if (!BeeUtils.isEmpty(max)) {
        obj.setMaxValue(max);
      }
    }
  }

  public static void setDefaultHorizontalAlignment(HasTextAlign obj, ValueType type) {
    Assert.notNull(obj);
    TextAlign align = getDefaultHorizontalAlignment(type);
    if (align != null) {
      obj.setTextAlign(align);
    }
  }

  public static void setHorizontalAlignment(Element elem, String text) {
    Assert.notNull(elem);
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    TextAlign align = StyleUtils.parseTextAlign(text);
    if (align != null) {
      StyleUtils.setTextAlign(elem, align);
    }
  }

  public static void setHorizontalAlignment(HasTextAlign obj, String text) {
    Assert.notNull(obj);
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    TextAlign align = StyleUtils.parseTextAlign(text);
    if (align != null) {
      obj.setTextAlign(align);
    }
  }

  public static void setVerticalAlignment(Element elem, String text) {
    Assert.notNull(elem);
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    VerticalAlign align = StyleUtils.parseVerticalAlign(text);
    if (align != null) {
      StyleUtils.setProperty(elem.getStyle(), CssProperties.VERTICAL_ALIGN, align);
    }
  }

  public static void setVerticalAlignment(HasVerticalAlign obj, String text) {
    Assert.notNull(obj);
    if (BeeUtils.isEmpty(text)) {
      return;
    }

    VerticalAlign align = StyleUtils.parseVerticalAlign(text);
    if (align != null) {
      obj.setVerticalAlign(align);
    }
  }

  public static void setWhiteSpace(HasWhiteSpace obj, String input) {
    Assert.notNull(obj);

    WhiteSpace whiteSpace = StyleUtils.parseWhiteSpace(input);
    if (whiteSpace != null) {
      obj.setWhiteSpace(whiteSpace);
    }
  }

  public static void setWidget(HasOneWidget container, Holder<Widget> holder,
      WidgetInitializer initializer, String name) {
    Assert.notNull(holder);
    holder.set(setWidget(container, holder.get(), initializer, name));
  }

  public static Widget setWidget(HasOneWidget container, Widget widget,
      WidgetInitializer initializer, String name) {
    if (container == null || widget == null) {
      return null;
    }

    if (initializer == null) {
      container.setWidget(widget);
      return widget;
    }

    Widget w = initializer.initialize(widget, name);
    if (w != null) {
      container.setWidget(w);
    }
    return w;
  }

  public static void updateForm(String widgetId, String columnId, String value) {
    Assert.notEmpty(widgetId);
    Assert.notEmpty(columnId);

    Widget widget = DomUtils.getWidget(widgetId);
    if (widget == null) {
      logger.severe("update form:", widgetId, "widget not found");
      return;
    }

    FormView form = getForm(widget);
    if (form == null) {
      logger.severe("update form:", widgetId, columnId, value, "form not found");
      return;
    }

    form.updateCell(columnId, value);
  }

  private static boolean isOrHasChild(Focusable widget, Element element) {
    return widget instanceof Widget && ((Widget) widget).getElement().isOrHasChild(element);
  }

  private UiHelper() {
  }
}
