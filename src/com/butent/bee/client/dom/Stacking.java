package com.butent.bee.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Stacking {

  private static final BeeLogger logger = LogUtils.getLogger(Stacking.class);

  private static final Map<String, Integer> widgetLevels = new HashMap<>();

  public static int addContext(Element el) {
    Assert.notNull(el, "Stacking add: element is null");
    int level = push(el.getId());
    el.getStyle().setZIndex(level);
    return level;
  }

  public static int addContext(UIObject obj) {
    Assert.notNull(obj, "Stacking add: ui object is null");
    return addContext(obj.getElement());
  }

  public static int bringToFront(Element el) {
    Assert.notNull(el, "Stacking bringToFront: element is null");

    int level = BeeConst.UNDEF;
    String id = el.getId();

    if (BeeUtils.isEmpty(id)) {
      logger.warning("Stacking bringToFront: element has no id");

    } else if (!widgetLevels.containsKey(id)) {
      logger.warning("Stacking bringToFront: id " + id + " not in widgetLevels");

    } else {
      level = widgetLevels.get(id);
      int max = getMaxLevel();

      if (max > level) {
        level = max + 1;
        widgetLevels.put(id, level);

        el.getStyle().setZIndex(level);
        logger.debug("bringToFront", id, level);
      }
    }

    return level;
  }

  public static int bringToFront(UIObject obj) {
    Assert.notNull(obj, "Stacking bringToFront: ui object is null");
    return bringToFront(obj.getElement());
  }

  public static void ensureParentContext(Element el) {
    Assert.notNull(el);
    Element parent = el.getParentElement();
    Assert.notNull(parent);

    String zIndex = parent.getStyle().getZIndex();
    if (!BeeUtils.isInt(zIndex)) {
      StyleUtils.setZIndex(parent, 0);
    }
  }

  public static void ensureParentContext(UIObject obj) {
    Assert.notNull(obj);
    ensureParentContext(obj.getElement());
  }

  public static List<Property> getInfo() {
    List<Property> lst = PropertyUtils.createProperties("Widget Levels",
        BeeUtils.bracket(widgetLevels.size()));

    for (Map.Entry<String, Integer> entry : widgetLevels.entrySet()) {
      PropertyUtils.addProperty(lst, entry.getKey(), entry.getValue());
    }
    return lst;
  }

  public static String peek() {
    if (!widgetLevels.isEmpty()) {
      int max = getMaxLevel();

      for (Map.Entry<String, Integer> entry : widgetLevels.entrySet()) {
        if (entry.getValue().equals(max)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  public static void removeContext(Element el) {
    Assert.notNull(el, "Stacking remove: element is null");
    pop(el.getId());
  }

  public static void removeContext(UIObject obj) {
    Assert.notNull(obj, "Stacking remove: ui object is null");
    removeContext(obj.getElement());
  }

  public static int size() {
    return widgetLevels.size();
  }

  private static int getMaxLevel() {
    int max = 0;

    if (!widgetLevels.isEmpty()) {
      for (int level : widgetLevels.values()) {
        if (level > max) {
          max = level;
        }
      }
    }

    return max;
  }

  private static void pop(String id) {
    Assert.notEmpty(id, "Stacking pop: id is empty");
    Integer level = widgetLevels.remove(id);

    if (level == null) {
      logger.severe("Stacking pop: id " + id + " not in widgetLevels");
      for (Property prop : getInfo()) {
        logger.debug(prop.getName(), prop.getValue());
      }
      logger.addSeparator();
    }
  }

  private static int push(String id) {
    Assert.notEmpty(id, "Stacking push: id is empty");

    int max = getMaxLevel();
    max++;

    widgetLevels.put(id, max);

    return max;
  }

  private Stacking() {
  }
}
