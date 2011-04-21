package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.utils.JsUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public class StyleUtils {

  public enum FontSize implements HasCssName {
    XX_SMALL {
      public String getCssName() {
        return FONT_SIZE_XX_SMALL;
      }
    },
    X_SMALL {
      public String getCssName() {
        return FONT_SIZE_X_SMALL;
      }
    },
    SMALL {
      public String getCssName() {
        return FONT_SIZE_SMALL;
      }
    },
    MEDIUM {
      public String getCssName() {
        return FONT_SIZE_MEDIUM;
      }
    },
    LARGE {
      public String getCssName() {
        return FONT_SIZE_LARGE;
      }
    },
    X_LARGE {
      public String getCssName() {
        return FONT_SIZE_X_LARGE;
      }
    },
    XX_LARGE {
      public String getCssName() {
        return FONT_SIZE_XX_LARGE;
      }
    },

    SMALLER {
      public String getCssName() {
        return FONT_SIZE_SMALLER;
      }
    },
    LARGER {
      public String getCssName() {
        return FONT_SIZE_LARGER;
      }
    }
  }

  public enum FontVariant implements HasCssName {
    NORMAL {
      public String getCssName() {
        return FONT_VARIANT_NORMAL;
      }
    },
    SMALL_CAPS {
      public String getCssName() {
        return FONT_VARIANT_SMALL_CAPS;
      }
    }
  }
  
  public enum ScrollBars {
    NONE, HORIZONTAL, VERTICAL, BOTH
  }
  
  public static final String ACTIVE_BLANK = "bee-activeBlank";
  public static final String ACTIVE_CONTENT = "bee-activeContent";
  public static final String CONFIG_PANEL = "bee-configPanel";
  public static final String DND_SOURCE = "bee-dndSource";
  public static final String DND_OVER = "bee-dndOver";
  public static final String WINDOW_CAPTION = "bee-WindowCaption";
  public static final String WINDOW_HEADER = "bee-WindowHeader";
  public static final String WINDOW_FOOTER = "bee-WindowFooter";

  public static final String STYLE_LEFT = "left";
  public static final String STYLE_WIDTH = "width";
  public static final String STYLE_RIGHT = "right";

  public static final String STYLE_TOP = "top";
  public static final String STYLE_HEIGHT = "height";
  public static final String STYLE_BOTTOM = "bottom";

  public static final String STYLE_BORDER_TOP = "borderTop";
  public static final String STYLE_BORDER_BOTTOM = "borderBottom";

  public static final String STYLE_TABLE_LAYOUT = "tableLayout";

  public static final String STYLE_OVERFLOW = "overflow";
  public static final String STYLE_OVERFLOW_X = "overflow-x";
  public static final String STYLE_OVERFLOW_Y = "overflow-y";

  public static final String STYLE_FONT_FAMILY = "fontFamily";
  public static final String STYLE_FONT_SIZE = "fontSize";
  public static final String STYLE_FONT_VARIANT = "fontVariant";
  
  public static final String VALUE_AUTO = "auto";
  public static final String VALUE_FIXED = "fixed";
  public static final String VALUE_HIDDEN = "hidden";
  public static final String VALUE_SCROLL = "scroll";

  public static final String NAME_HORIZONTAL = "horizontal";
  public static final String NAME_VERTICAL = "vertical";
  public static final String NAME_DISABLED = "disabled";
  public static final String NAME_ENABLED = "enabled";
  public static final String NAME_FOCUSED = "focused";

  public static final String NAME_UNSELECTABLE = "unselectable";
  
  public static final String FONT_SIZE_XX_SMALL = "xx-small";
  public static final String FONT_SIZE_X_SMALL = "x-small";
  public static final String FONT_SIZE_SMALL = "small";
  public static final String FONT_SIZE_MEDIUM = "medium";
  public static final String FONT_SIZE_LARGE = "large";
  public static final String FONT_SIZE_X_LARGE = "x-large";
  public static final String FONT_SIZE_XX_LARGE = "xx-large";

  public static final String FONT_SIZE_SMALLER = "smaller";
  public static final String FONT_SIZE_LARGER = "larger";

  public static final String FONT_VARIANT_NORMAL = "normal";
  public static final String FONT_VARIANT_SMALL_CAPS = "small-caps";

  public static void addStyleDependentName(Element el, String styleSuffix) {
    setStyleDependentName(el, styleSuffix, true);
  }

  public static void alwaysScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    alwaysScroll(obj.getElement(), scroll);
  }
  
  public static void alwaysScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    alwaysScroll(el.getStyle(), scroll);
  }
  
  public static void alwaysScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_SCROLL);
  }
  
  public static void autoHeight(UIObject obj) {
    Assert.notNull(obj);
    autoHeight(obj.getElement());
  }

  public static void autoHeight(Element el) {
    Assert.notNull(el);
    autoHeight(el.getStyle());
  }

  public static void autoHeight(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_HEIGHT, VALUE_AUTO);
  }

  public static void autoScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    autoScroll(obj.getElement(), scroll);
  }
  
  public static void autoScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    autoScroll(el.getStyle(), scroll);
  }
  
  public static void autoScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_AUTO);
  }

  public static void autoWidth(UIObject obj) {
    Assert.notNull(obj);
    autoWidth(obj.getElement());
  }

  public static void autoWidth(Element el) {
    Assert.notNull(el);
    autoWidth(el.getStyle());
  }

  public static void autoWidth(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_WIDTH, VALUE_AUTO);
  }

  public static void clearTableLayout(UIObject obj) {
    Assert.notNull(obj);
    clearTableLayout(obj.getElement());
  }

  public static void clearTableLayout(Element el) {
    Assert.notNull(el);
    clearTableLayout(el.getStyle());
  }

  public static void clearTableLayout(Style st) {
    Assert.notNull(st);
    st.clearProperty(STYLE_TABLE_LAYOUT);
  }

  public static void fillHorizontal(UIObject obj) {
    Assert.notNull(obj);
    fillHorizontal(obj.getElement());
  }

  public static void fillHorizontal(Element el) {
    Assert.notNull(el);
    fillHorizontal(el.getStyle());
  }

  public static void fillHorizontal(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getLeft())) {
      st.setLeft(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_RIGHT)) {
      st.clearRight();
    }

    st.setWidth(100, Unit.PCT);
  }

  public static void fillVertical(UIObject obj) {
    Assert.notNull(obj);
    fillVertical(obj.getElement());
  }

  public static void fillVertical(Element el) {
    Assert.notNull(el);
    fillVertical(el.getStyle());
  }

  public static void fillVertical(Style st) {
    Assert.notNull(st);

    if (!isZero(st.getTop())) {
      st.setTop(0, Unit.PX);
    }
    if (hasProperty(st, STYLE_BOTTOM)) {
      st.clearBottom();
    }

    st.setHeight(100, Unit.PCT);
  }

  public static void fixedTableLayout(UIObject obj) {
    Assert.notNull(obj);
    fixedTableLayout(obj.getElement());
  }

  public static void fixedTableLayout(Element el) {
    Assert.notNull(el);
    fixedTableLayout(el.getStyle());
  }

  public static void fixedTableLayout(Style st) {
    Assert.notNull(st);
    st.setProperty(STYLE_TABLE_LAYOUT, VALUE_FIXED);
  }

  public static void fullHeight(UIObject obj) {
    Assert.notNull(obj);
    fullHeight(obj.getElement());
  }

  public static void fullHeight(Element el) {
    Assert.notNull(el);
    fullHeight(el.getStyle());
  }

  public static void fullHeight(Style st) {
    Assert.notNull(st);
    st.setHeight(100, Unit.PCT);
  }

  public static void fullWidth(UIObject obj) {
    Assert.notNull(obj);
    fullWidth(obj.getElement());
  }

  public static void fullWidth(Element el) {
    Assert.notNull(el);
    fullWidth(el.getStyle());
  }

  public static void fullWidth(Style st) {
    Assert.notNull(st);
    st.setWidth(100, Unit.PCT);
  }

  public static Element getElement(String id) {
    Assert.notEmpty(id);
    Element el = DOM.getElementById(id);
    Assert.notNull(el, "id " + id + " element not found");
    return el;
  }

  public static ScrollBars getScroll(UIObject obj) {
    Assert.notNull(obj);
    return getScroll(obj.getElement());
  }
  
  public static ScrollBars getScroll(Element el) {
    Assert.notNull(el);
    return getScroll(el.getStyle());
  }
  
  public static ScrollBars getScroll(Style st) {
    Assert.notNull(st);
    
    if (isScroll(st.getOverflow())) {
      return ScrollBars.BOTH;
    }
    if (isScroll(getStyleProperty(st, STYLE_OVERFLOW_X))) {
      return ScrollBars.HORIZONTAL;
    }
    if (isScroll(getStyleProperty(st, STYLE_OVERFLOW_Y))) {
      return ScrollBars.VERTICAL;
    }
    return ScrollBars.NONE;
  }

  public static String getStylePrimaryName(Element el) {
    Assert.notNull(el);
    String className = el.getClassName();
    if (BeeUtils.isEmpty(className)) {
      return BeeConst.STRING_EMPTY;
    }

    int idx = className.indexOf(BeeConst.CHAR_SPACE);
    if (idx >= 0) {
      return className.substring(0, idx);
    }
    return className;
  }

  public static int getTop(String id) {
    return getTop(getElement(id));
  }

  public static int getTop(UIObject obj) {
    Assert.notNull(obj);
    return getTop(obj.getElement());
  }

  public static int getTop(Element el) {
    Assert.notNull(el);
    return getTop(el.getStyle());
  }

  public static int getTop(Style st) {
    Assert.notNull(st);
    return BeeUtils.val(st.getTop());
  }

  public static void hideScroll(UIObject obj, ScrollBars scroll) {
    Assert.notNull(obj);
    hideScroll(obj.getElement(), scroll);
  }
  
  public static void hideScroll(Element el, ScrollBars scroll) {
    Assert.notNull(el);
    hideScroll(el.getStyle(), scroll);
  }
  
  public static void hideScroll(Style st, ScrollBars scroll) {
    setOverflow(st, scroll, VALUE_HIDDEN);
  }
  
  public static <E extends Enum<?> & HasCssName> E parseCssName(Class<E> clazz, String input) {
    Assert.notNull(clazz);
    Assert.notEmpty(input);
    
    for (E constant : clazz.getEnumConstants()) {
      if (BeeUtils.same(constant.getCssName(), input)) {
        return constant;
      }
    }
    return null;
  }

  public static FontSize parseFontSize(String input) {
    return parseCssName(FontSize.class, input);
  }

  public static FontStyle parseFontStyle(String input) {
    return parseCssName(FontStyle.class, input);
  }

  public static FontVariant parseFontVariant(String input) {
    return parseCssName(FontVariant.class, input);
  }
  
  public static FontWeight parseFontWeight(String input) {
    return parseCssName(FontWeight.class, input);
  }
  
  public static Unit parseUnit(String input) {
    Assert.notEmpty(input);

    for (Unit constant : Unit.class.getEnumConstants()) {
      if (BeeUtils.same(constant.getType(), input)) {
        return constant;
      }
    }
    return null;
  }
 
  public static void removeStyleDependentName(Element el, String styleSuffix) {
    setStyleDependentName(el, styleSuffix, false);
  }

  public static void setBorderBottomWidth(UIObject obj, int px) {
    setBorderBottomWidth(obj.getElement(), px);
  }

  public static void setBorderBottomWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderBottomWidth(el.getStyle(), px);
  }

  public static void setBorderBottomWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_BOTTOM, px);
  }

  public static void setBorderTopWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setBorderTopWidth(obj.getElement(), px);
  }

  public static void setBorderTopWidth(Element el, int px) {
    Assert.notNull(el);
    setBorderTopWidth(el.getStyle(), px);
  }

  public static void setBorderTopWidth(Style st, int px) {
    Assert.notNull(st);
    Assert.nonNegative(px);
    st.setPropertyPx(STYLE_BORDER_TOP, px);
  }

  public static void setFontFamily(UIObject obj, String family) {
    Assert.notNull(obj);
    setFontFamily(obj.getElement(), family);
  }

  public static void setFontFamily(Element el, String family) {
    Assert.notNull(el);
    setFontFamily(el.getStyle(), family);
  }
  
  public static void setFontFamily(Style st, String family) {
    Assert.notNull(st);
    Assert.notEmpty(family);
    st.setProperty(STYLE_FONT_FAMILY, family);
  }
  
  public static void setFontSize(UIObject obj, FontSize size) {
    Assert.notNull(obj);
    setFontSize(obj.getElement(), size);
  }

  public static void setFontSize(Element el, FontSize size) {
    Assert.notNull(el);
    setFontSize(el.getStyle(), size);
  }

  public static void setFontSize(UIObject obj, double size, Unit unit) {
    Assert.notNull(obj);
    setFontSize(obj.getElement(), size, unit);
  }
  
  public static void setFontSize(Element el, double size, Unit unit) {
    Assert.notNull(el);
    Assert.isPositive(size);
    Assert.notNull(unit);
    el.getStyle().setFontSize(size, unit);
  }
  
  public static void setFontSize(Style st, FontSize size) {
    Assert.notNull(st);
    Assert.notNull(size);
    setFontSize(st, size.getCssName());
  }

  public static void setFontSizePx(UIObject obj, double size) {
    Assert.notNull(obj);
    setFontSizePx(obj.getElement(), size);
  }
  
  public static void setFontSizePx(Element el, double size) {
    Assert.notNull(el);
    setFontSizePx(el.getStyle(), size);
  }
  
  public static void setFontSizePx(Style st, double size) {
    Assert.notNull(st);
    Assert.isPositive(size);
    st.setFontSize(size, Unit.PX);
  }

  public static void setFontVariant(UIObject obj, FontVariant variant) {
    Assert.notNull(obj);
    setFontVariant(obj.getElement(), variant);
  }

  public static void setFontVariant(Element el, FontVariant variant) {
    Assert.notNull(el);
    setFontVariant(el.getStyle(), variant);
  }
  
  public static void setFontVariant(Style st, FontVariant variant) {
    Assert.notNull(st);
    Assert.notNull(variant);
    st.setProperty(STYLE_FONT_VARIANT, variant.getCssName());
  }
  
  public static void setHeight(UIObject obj, int px) {
    Assert.notNull(obj);
    setHeight(obj.getElement(), px);
  }

  public static void setHeight(Element el, int px) {
    Assert.notNull(el);
    setHeight(el.getStyle(), px);
  }

  public static void setHeight(Style st, int px) {
    Assert.notNull(st);
    st.setHeight(px, Unit.PX);
  }

  public static void setLeft(UIObject obj, int px) {
    Assert.notNull(obj);
    setLeft(obj.getElement(), px);
  }

  public static void setLeft(Element el, int px) {
    Assert.notNull(el);
    setLeft(el.getStyle(), px);
  }

  public static void setLeft(Style st, int px) {
    Assert.notNull(st);
    st.setLeft(px, Unit.PX);
  }

  public static void setOverflow(UIObject obj, ScrollBars scroll, String value) {
    Assert.notNull(obj);
    setOverflow(obj.getElement(), scroll, value);
  }
  
  public static void setOverflow(Element el, ScrollBars scroll, String value) {
    Assert.notNull(el);
    setOverflow(el.getStyle(), scroll, value);
  }
  
  public static void setOverflow(Style st, ScrollBars scroll, String value) {
    Assert.notNull(st);
    Assert.notNull(scroll);
    Assert.notEmpty(value);
    
    switch (scroll) {
      case BOTH:
        st.setProperty(STYLE_OVERFLOW, value);
        break;
      case HORIZONTAL:
        setStyleProperty(st, STYLE_OVERFLOW_X, value);
        break;
      case VERTICAL:
        setStyleProperty(st, STYLE_OVERFLOW_Y, value);
        break;
      case NONE:
        clearStyleProperty(st, STYLE_OVERFLOW);
        clearStyleProperty(st, STYLE_OVERFLOW_X);
        clearStyleProperty(st, STYLE_OVERFLOW_Y);
        break;
      default:
        Assert.untouchable();
    }
  }
  
  public static void setStyleDependentName(Element el, String styleSuffix, boolean add) {
    Assert.notNull(el);
    Assert.notEmpty(styleSuffix);

    String primary = getStylePrimaryName(el);
    Assert.notEmpty(primary, "element has no primary style");

    setStyleName(el, primary + BeeConst.CHAR_MINUS + styleSuffix.trim(), add);
  }

  public static void setStyleName(Element el, String st, boolean add) {
    Assert.notNull(el);
    Assert.notEmpty(st);

    if (add) {
      el.addClassName(st.trim());
    } else {
      el.removeClassName(st.trim());
    }
  }

  public static void setTop(String id, int px) {
    setTop(getElement(id), px);
  }

  public static void setTop(UIObject obj, int px) {
    Assert.notNull(obj);
    setTop(obj.getElement(), px);
  }

  public static void setTop(Element el, int px) {
    Assert.notNull(el);
    setTop(el.getStyle(), px);
  }

  public static void setTop(Style st, int px) {
    Assert.notNull(st);
    st.setTop(px, Unit.PX);
  }

  public static void setWidth(UIObject obj, int px) {
    Assert.notNull(obj);
    setWidth(obj.getElement(), px);
  }

  public static void setWidth(Element el, int px) {
    Assert.notNull(el);
    setWidth(el.getStyle(), px);
  }

  public static void setWidth(Style st, int px) {
    Assert.notNull(st);
    st.setWidth(px, Unit.PX);
  }

  public static void zeroLeft(UIObject obj) {
    Assert.notNull(obj);
    zeroLeft(obj.getElement());
  }

  public static void zeroLeft(Element el) {
    Assert.notNull(el);
    zeroLeft(el.getStyle());
  }

  public static void zeroLeft(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_LEFT, 0);
  }

  public static void zeroTop(UIObject obj) {
    Assert.notNull(obj);
    zeroTop(obj.getElement());
  }

  public static void zeroTop(Element el) {
    Assert.notNull(el);
    zeroTop(el.getStyle());
  }

  public static void zeroTop(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_TOP, 0);
  }

  public static void zeroWidth(UIObject obj) {
    Assert.notNull(obj);
    zeroWidth(obj.getElement());
  }

  public static void zeroWidth(Element el) {
    Assert.notNull(el);
    zeroWidth(el.getStyle());
  }

  public static void zeroWidth(Style st) {
    Assert.notNull(st);
    st.setPropertyPx(STYLE_WIDTH, 0);
  }

  private static void clearStyleProperty(Style style, String name) {
    if (!BeeUtils.isEmpty(getStyleProperty(style, name))) {
      setStyleProperty(style, name, BeeConst.STRING_EMPTY);
    }
  }
  
  private static String getStyleProperty(Style style, String name) {
    return JsUtils.getProperty(style, name);
  }
  
  private static boolean hasProperty(Style st, String name) {
    if (st == null || BeeUtils.isEmpty(name)) {
      return false;
    } else {
      return !BeeUtils.isEmpty(st.getProperty(name));
    }
  }
  
  private static boolean isScroll(String value) {
    return BeeUtils.inListSame(value, VALUE_AUTO, VALUE_SCROLL);
  }

  private static boolean isZero(String s) {
    if (BeeUtils.isEmpty(s)) {
      return false;
    }
    if (s.trim().charAt(0) != BeeConst.CHAR_ZERO) {
      return false;
    }
    boolean ok = true;

    for (int i = 1; i < s.trim().length(); i++) {
      char z = s.trim().charAt(i);

      if (z == BeeConst.CHAR_ZERO || z == BeeConst.CHAR_SPACE || z == BeeConst.CHAR_POINT) {
        continue;
      } else {
        if (Character.isDigit(z)) {
          ok = false;
        }
        break;
      }
    }
    return ok;
  }

  private static void setFontSize(Style st, String value) {
    Assert.notNull(st);
    Assert.notEmpty(value);
    st.setProperty(STYLE_FONT_SIZE, value);
  }
  
  private static void setStyleProperty(Style style, String name, String value) {
    JsUtils.setProperty(style, name, value);
  }

  private StyleUtils() {
  }
}
