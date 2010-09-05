package com.butent.bee.egg.client;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.client.layout.BeeStack;
import com.butent.bee.egg.client.layout.BeeTab;
import com.butent.bee.egg.client.menu.BeeMenuBar;
import com.butent.bee.egg.client.menu.BeeMenuItem;
import com.butent.bee.egg.client.menu.BeeMenuItemSeparator;
import com.butent.bee.egg.client.menu.MenuCommand;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.tree.BeeTreeItem;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.widget.BeeListBox;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.menu.MenuConst;
import com.butent.bee.egg.shared.menu.MenuEntry;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

public class BeeMenu implements BeeModule {
  private List<MenuEntry> roots = null;
  private List<MenuEntry> items = null;

  private String rootLayout = MenuConst.DEFAULT_ROOT_LAYOUT;
  private String itemLayout = MenuConst.DEFAULT_ITEM_LAYOUT;

  private BeeMenuItemSeparator defaultItemSeparator = new BeeMenuItemSeparator();
  
  private int rootLimit = BeeConst.SIZE_UNKNOWN;
  private int itemLimit = BeeConst.SIZE_UNKNOWN;

  public BeeMenu() {
    super();
  }

  public String getName() {
    return getClass().getName();
  }

  public int getPriority(int p) {
    switch (p) {
    case PRIORITY_INIT:
      return DO_NOT_CALL;
    case PRIORITY_START:
      return 20;
    case PRIORITY_END:
      return DO_NOT_CALL;
    default:
      return DO_NOT_CALL;
    }
  }

  public void init() {
  }

  public void start() {
    clear();
    loadMenu();
  }

  public void end() {
  }

  public List<MenuEntry> getRoots() {
    return roots;
  }

  public void setRoots(List<MenuEntry> roots) {
    this.roots = roots;
  }

  public List<MenuEntry> getItems() {
    return items;
  }

  public void setItems(List<MenuEntry> items) {
    this.items = items;
  }

  public String getRootLayout() {
    return rootLayout;
  }

  public void setRootLayout(String rootLayout) {
    this.rootLayout = rootLayout;
  }

  public String getItemLayout() {
    return itemLayout;
  }

  public void setItemLayout(String itemLayout) {
    this.itemLayout = itemLayout;
  }

  public BeeMenuItemSeparator getDefaultItemSeparator() {
    return defaultItemSeparator;
  }

  public void setDefaultItemSeparator(BeeMenuItemSeparator defaultItemSeparator) {
    this.defaultItemSeparator = defaultItemSeparator;
  }

  public int getRootLimit() {
    return rootLimit;
  }

  public void setRootLimit(int rootLimit) {
    this.rootLimit = rootLimit;
  }

  public int getItemLimit() {
    return itemLimit;
  }

  public void setItemLimit(int itemLimit) {
    this.itemLimit = itemLimit;
  }

  public void loadMenu() {
    BeeKeeper.getRpc().dispatchService(BeeService.SERVICE_GET_MENU);
  }

  public void loadCallBack(JsArrayString arr) {
    Assert.notNull(arr);
    int n = arr.length();
    Assert.isPositive(n);

    BeeDuration dur = new BeeDuration("load menu");

    clear();
    MenuEntry entry;

    int rc = 0;
    int ic = 0;

    for (int i = 0; i < n; i++) {
      entry = new MenuEntry();
      entry.deserialize(arr.get(i));

      if (!entry.isValid()) {
        BeeKeeper.getLog().severe("invalid menu entry", BeeUtils.bracket(i),
            arr.get(i));
        break;
      }

      if (entry.isRoot()) {
        roots.add(entry);
        rc++;
      } else {
        items.add(entry);
        ic++;
      }
    }

    BeeKeeper.getLog().finish(dur, BeeUtils.addName("roots", rc),
        BeeUtils.addName("items", ic));
  }

  public boolean drawMenu() {
    Assert.state(validState());

    String rl = BeeGlobal.getFieldValue(MenuConst.FIELD_ROOT_LAYOUT);
    String il = BeeGlobal.getFieldValue(MenuConst.FIELD_ITEM_LAYOUT);

    Assert.isTrue(MenuConst.isValidLayout(rl));
    Assert.isTrue(MenuConst.isValidLayout(il));

    BeeDuration dur = new BeeDuration("draw menu");
    Widget w = createMenu(rl, il);
    BeeKeeper.getLog().finish(dur);

    boolean ok = (w != null);
    if (ok) {
      setRootLayout(rl);
      setItemLayout(il);
      BeeKeeper.getUi().updateMenu(w);
    } else {
      BeeKeeper.getLog().severe("error creating menu");
    }

    return ok;
  }

  public boolean validState() {
    return BeeUtils.allNotEmpty(getRoots(), getItems());
  }

  public void showMenu() {
    if (BeeUtils.allEmpty(getRoots(), getItems())) {
      BeeGlobal.showDialog("menu empty");
      return;
    }

    String[] cols = new String[] { "id", "parent", "order", "sep", "text",
        "service", "parameters", "type", "style", "key", "visible" };

    int rc = getRootCount();
    int ic = getItemCount();

    String[][] arr = new String[rc + ic][cols.length];
    MenuEntry entry;
    int j;

    for (int i = 0; i < rc + ic; i++) {
      if (i < rc) {
        entry = getRoots().get(i);
      } else {
        entry = getItems().get(i - rc);
      }
      
      j = 0;
      
      arr[i][j++] = entry.getId();
      arr[i][j++] = entry.getParent();
      arr[i][j++] = BeeUtils.transform(entry.getOrder());
      arr[i][j++] = BeeUtils.transform(entry.getSeparators());
      arr[i][j++] = entry.getText();
      arr[i][j++] = entry.getService();
      arr[i][j++] = entry.getParameters();
      arr[i][j++] = entry.getType();
      arr[i][j++] = entry.getStyle();
      arr[i][j++] = entry.getKeyName();
      arr[i][j++] = BeeUtils.toString(entry.isVisible());
    }
    
    BeeKeeper.getUi().updateActivePanel(BeeGlobal.createSimpleGrid(cols, arr));
  }

  private Widget createMenu(String rl, String il) {
    Widget rw = createRootWidget(rl);

    List<MenuEntry> children;
    Widget cw;
    
//    int rLim = getRootLimit();
//    int iLim = getItemLimit();
    
    int rLim = 5;
    int iLim = 15;

    int rc = 0;
    int ic;

    for (MenuEntry root : getRoots()) {
      children = getChildren(root.getId());

      if (BeeUtils.isEmpty(children)) {
        cw = null;
      } else {
        cw = createItemWidget(il);
        
        ic = 0;
        for (MenuEntry item : children) {
          addItem(cw, item);
          ic++;
          if (iLim > 0 && ic >= iLim) {
            break;
          }
        }
      }

      addRoot(rw, root, cw);
      rc++;
      
      if (rLim > 0 && rc >= rLim) {
        break;
      }
    }

    return rw;
  }

  private Widget createRootWidget(String layout) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConst.LAYOUT_HORIZONTAL)) {
      w = new BeeMenuBar();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_VERTICAL)) {
      w = new BeeMenuBar(true);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_STACK)) {
      w = new BeeStack(Unit.EM);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
      w = new BeeTree();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TAB)) {
      w = new BeeTab(20, Unit.PX);
    } else {
      w = new BeeMenuBar();
    }

    return w;
  }

  private Widget createItemWidget(String layout) {
    Widget w = null;

    if (BeeUtils.same(layout, MenuConst.LAYOUT_HORIZONTAL)) {
      w = new BeeMenuBar();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_HORIZONTAL)) {
      w = new BeeMenuBar(true);
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_TREE)) {
      w = new BeeTree();
    } else if (BeeUtils.same(layout, MenuConst.LAYOUT_LIST)) {
      w = new BeeListBox();
    } else {
      w = new BeeMenuBar(true);
    }

    return w;
  }

  private void addItem(Widget w, MenuEntry item) {
    String txt = item.getText();
    String svc = item.getService();
    String opt = item.getParameters();

    boolean sep_before = MenuConst.isSeparatorBefore(item.getSeparators());
    boolean sep_after = MenuConst.isSeparatorAfter(item.getSeparators());

    if (w instanceof BeeMenuBar) {
      if (sep_before) {
        ((BeeMenuBar) w).addSeparator(getDefaultItemSeparator());
      }

      ((BeeMenuBar) w).addItem(new BeeMenuItem(txt, new MenuCommand(svc, opt)));

      if (sep_after) {
        ((BeeMenuBar) w).addSeparator(getDefaultItemSeparator());
      }

    } else if (w instanceof BeeListBox) {
      ((BeeListBox) w).addItem(txt);

    } else if (w instanceof BeeTree) {
      ((BeeTree) w).addItem(txt);
    }

  }

  private void addRoot(Widget rw, MenuEntry item, Widget cw) {
    String txt = item.getText();
    String svc = item.getService();
    String opt = item.getParameters();

    if (rw instanceof BeeMenuBar) {
      if (cw == null) {
        ((BeeMenuBar) rw).addItem(new BeeMenuItem(txt,
            new MenuCommand(svc, opt)));
      } else if (cw instanceof BeeMenuBar) {
        ((BeeMenuBar) rw).addItem(new BeeMenuItem(txt, (BeeMenuBar) cw));
      }
    }

    else if (rw instanceof BeeStack) {
      if (cw != null) {
        ((BeeStack) rw).add(cw, txt, 2);
      }
    }

    else if (rw instanceof BeeTree) {
      BeeTreeItem it = new BeeTreeItem(txt);
      if (cw != null) {
        it.addItem(cw);
      }
      ((BeeTree) rw).addItem(it);
    }
    
    else if (rw instanceof BeeTab) {
      if (cw != null) {
        ((BeeTab) rw).add(cw, txt);
      }
    }

  }

  private List<MenuEntry> getChildren(String id) {
    List<MenuEntry> lst = new ArrayList<MenuEntry>();
    boolean tg = false;

    for (MenuEntry entry : items) {
      if (BeeUtils.same(entry.getParent(), id)) {
        lst.add(entry);
        tg = true;
      } else if (tg) {
        break;
      }
    }

    return lst;
  }

  private void clear() {
    roots = new ArrayList<MenuEntry>();
    items = new ArrayList<MenuEntry>();
  }

  private int getRootCount() {
    return (getRoots() == null) ? 0 : getRoots().size();
  }

  private int getItemCount() {
    return (getItems() == null) ? 0 : getItems().size();
  }

}
