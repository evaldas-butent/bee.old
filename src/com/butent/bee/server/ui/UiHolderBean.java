package com.butent.bee.server.ui;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeObject;
import com.butent.bee.shared.data.DataNameProvider;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;
import com.butent.bee.shared.menu.MenuService;
import com.butent.bee.shared.modules.administration.SysObject;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.RightsState;
import com.butent.bee.shared.rights.RightsUtils;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Initializes and uses <code>UiLoaderBean</code> for dynamic interface generation from a data
 * source.
 */

@Singleton
@Lock(LockType.READ)
public class UiHolderBean {

  private static final class UiObjectInfo implements BeeObject {

    private final String module;
    private final String name;
    private final String resource;
    private String viewName;
    boolean viewSet;

    private UiObjectInfo(String module, String name, String resource) {
      this.module = Assert.notEmpty(module);
      this.name = Assert.notEmpty(name);
      this.resource = Assert.notEmpty(resource);
    }

    @Override
    public String getModule() {
      return module;
    }

    @Override
    public String getName() {
      return name;
    }

    private String getResource() {
      return resource;
    }

    private String getViewName() {
      return viewName;
    }

    private boolean isViewSet() {
      return viewSet;
    }

    private void setViewName(String viewName) {
      this.viewName = viewName;
      this.viewSet = true;
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(UiHolderBean.class);

  private static boolean isEmbeddedGrid(String name) {
    return BeeUtils.inListSame(name, UiConstants.TAG_CHILD_GRID, UiConstants.TAG_GRID_PANEL);
  }

  private static boolean isHidable(Element element) {
    for (Element parent = element; parent != null; parent = XmlUtils.getParentElement(parent)) {
      Boolean visible = XmlUtils.getAttributeBoolean(parent, UiConstants.ATTR_VISIBLE);
      if (BeeUtils.isTrue(visible)) {
        return false;
      }
    }
    return true;
  }

  private static String key(String name) {
    return BeeUtils.normalize(name);
  }

  private static List<Menu> maybeTransform(Menu item) {
    MenuService service;
    if (item instanceof MenuItem) {
      service = ((MenuItem) item).getService();
    } else {
      service = null;
    }

    if (service != null && service.getTransformer() != null) {
      return service.getTransformer().apply(item);
    } else {
      return Collections.singletonList(item);
    }
  }

  @EJB
  ModuleHolderBean moduleBean;
  @EJB
  GridLoaderBean gridBean;
  @EJB
  UserServiceBean usr;
  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  ParamHolderBean prm;

  private final Map<String, UiObjectInfo> gridCache = new HashMap<>();
  private final Map<String, UiObjectInfo> formCache = new HashMap<>();
  private final Map<String, Menu> menuCache = new HashMap<>();
  private final Map<String, UiObjectInfo> reportCache = new HashMap<>();

  public void checkWidgetChildrenVisibility(Element parent, Set<String> hiddenColumns) {
    checkWidgetChildrenVisibility(parent, null, hiddenColumns);
  }

  public ResponseObject getForm(String formName) {
    Document doc = getFormDocument(formName, true);

    if (doc == null) {
      return ResponseObject.error("Not a form:", formName);
    }
    Element formElement = doc.getDocumentElement();

    if (!BeeUtils.same(formElement.getAttribute(UiConstants.ATTR_NAME), formName)) {
      return ResponseObject.error("From name doesn't match resource name:",
          formElement.getAttribute(UiConstants.ATTR_NAME), "!=", formName);
    }
    String viewName = getFormViewName(formName);
    BeeView view = sys.isView(viewName) ? sys.getView(viewName) : null;

    checkWidgetChildrenVisibility(formElement, view, getHiddenColumns());

    return ResponseObject.response(XmlUtils.toString(doc, false));
  }

  public DataNameProvider getFormDataNameProvider() {
    return input -> {
      Set<String> result = new HashSet<>();
      String viewName = getFormViewName(input);
      if (!BeeUtils.isEmpty(viewName)) {
        result.add(viewName);
      }
      return result;
    };
  }

  public Document getFormDocument(String formName, boolean respectSchema) {
    if (!isForm(formName)) {
      return null;
    }
    return XmlUtils.getXmlResource(formCache.get(key(formName)).getResource(),
        respectSchema ? Config.getSchemaPath(SysObject.FORM.getSchemaName()) : null);
  }

  public ResponseObject getGrid(String gridName) {
    if (!isGrid(gridName)) {
      return ResponseObject.error("Not a grid:", gridName);
    }
    String resource = gridCache.get(key(gridName)).getResource();
    Document doc = XmlUtils.getXmlResource(resource,
        Config.getSchemaPath(SysObject.GRID.getSchemaName()));

    if (doc == null) {
      return ResponseObject.error("Cannot parse xml:", resource);
    }
    Element gridElement = doc.getDocumentElement();

    if (!BeeUtils.same(gridElement.getAttribute(UiConstants.ATTR_NAME), gridName)) {
      return ResponseObject.error("Grid name doesn't match resource name:",
          gridElement.getAttribute(UiConstants.ATTR_NAME), "!=", gridName);
    }
    GridDescription grid = gridBean.getGridDescription(gridElement, getHiddenColumns());

    if (grid == null) {
      return ResponseObject.error("Cannot create grid description:", gridName);
    } else {
      return ResponseObject.response(grid);
    }
  }

  public DataNameProvider getGridDataNameProvider() {
    return input -> {
      Set<String> result = new HashSet<>();
      String viewName = getGridViewName(input);
      if (!BeeUtils.isEmpty(viewName)) {
        result.add(viewName);
      }
      return result;
    };
  }

  public ResponseObject getMenu(boolean checkRights, boolean transform) {
    Map<Integer, Menu> menus = new TreeMap<>();

    for (Menu menu : menuCache.values()) {
      Menu entry = getMenu(null, menu.copy(), checkRights, transform);

      if (entry != null) {
        menus.put(Assert.notContain(menus, entry.getOrder()), entry);
      }
    }
    return ResponseObject.response(menus.values());
  }

  public Collection<? extends BeeObject> getObjects(SysObject type) {
    switch (type) {
      case MENU:
        return menuCache.values();
      case GRID:
        return gridCache.values();
      case FORM:
        return formCache.values();
      case REPORT:
        return reportCache.values();
      default:
        Assert.unsupported();
        return null;
    }
  }

  public String getReport(String reportName) {
    if (!isReport(reportName)) {
      return null;
    }
    return reportCache.get(key(reportName)).getResource();
  }

  @Lock(LockType.WRITE)
  public void init() {
    initGrids();
    initForms();
    initMenu();
    initReports();

    MenuService.GRID.setDataNameProvider(getGridDataNameProvider());
    MenuService.FORM.setDataNameProvider(getFormDataNameProvider());
  }

  @Lock(LockType.WRITE)
  public void initForms() {
    initObjects(SysObject.FORM);
  }

  @Lock(LockType.WRITE)
  public void initGrids() {
    initObjects(SysObject.GRID);
  }

  @Lock(LockType.WRITE)
  public void initMenu() {
    initObjects(SysObject.MENU);

    Set<String> children = new HashSet<>();

    for (String menuKey : menuCache.keySet()) {
      Menu xmlMenu = menuCache.get(menuKey);
      String parent = xmlMenu.getParent();

      if (!BeeUtils.isEmpty(parent)) {
        Menu parentMenu = null;

        for (String entry : Splitter.on('.').omitEmptyStrings().trimResults().split(parent)) {
          if (parentMenu == null) {
            parentMenu = menuCache.get(key(entry));

          } else if (parentMenu instanceof MenuEntry) {
            boolean found = false;

            for (Menu item : ((MenuEntry) parentMenu).getItems()) {
              found = BeeUtils.same(item.getName(), entry);

              if (found) {
                parentMenu = item;
                break;
              }
            }
            if (!found) {
              parentMenu = null;
              break;
            }
          } else {
            break;
          }
        }
        if (parentMenu == null || !(parentMenu instanceof MenuEntry)) {
          logger.severe("Menu parent is not valid:", "Module:", xmlMenu.getModule(),
              "; Menu:", xmlMenu.getName(), "; Parent:", parent);
        } else {
          List<Menu> items = ((MenuEntry) parentMenu).getItems();
          Integer order = xmlMenu.getOrder();
          int index = BeeConst.UNDEF;

          if (BeeUtils.isNonNegative(order)) {
            if (BeeUtils.isIndex(items, order)) {
              index = order;
            } else {
              for (int i = 0; i < items.size(); i++) {
                if (BeeUtils.isMeq(items.get(i).getOrder(), order)) {
                  index = i;
                  break;
                }
              }
            }
          }
          if (BeeUtils.isIndex(items, index)) {
            items.add(index, xmlMenu);
          } else {
            items.add(xmlMenu);
          }
        }
        children.add(menuKey);
      }
    }
    for (String child : children) {
      menuCache.remove(child);
    }
  }

  @Lock(LockType.WRITE)
  public void initReports() {
    initObjects(SysObject.REPORT);
  }

  public boolean isForm(String formName) {
    return !BeeUtils.isEmpty(formName) && formCache.containsKey(key(formName));
  }

  public boolean isGrid(String gridName) {
    return !BeeUtils.isEmpty(gridName) && gridCache.containsKey(key(gridName));
  }

  public boolean isReport(String reportName) {
    return !BeeUtils.isEmpty(reportName) && reportCache.containsKey(key(reportName));
  }

  private void checkWidgetChildrenVisibility(Element parent, BeeView view,
      Set<String> hiddenColumns) {

    List<Element> elements = XmlUtils.getAllDescendantElements(parent);
    boolean visible;

    for (Element element : elements) {
      if (element.hasAttribute(UiConstants.ATTR_VISIBLE)) {
        visible = !BeeConst.isFalse(element.getAttribute(UiConstants.ATTR_VISIBLE));
      } else {
        visible = isWidgetVisible(element, view, hiddenColumns);
      }

      if (!visible && isHidable(XmlUtils.getParentElement(element))) {
        XmlUtils.removeFromParent(element);
      }
    }
  }

  private String getFormViewName(String formName) {
    if (!isForm(formName)) {
      return null;
    }
    UiObjectInfo formInfo = formCache.get(key(formName));

    if (!formInfo.isViewSet()) {
      Document doc = XmlUtils.getXmlResource(formInfo.getResource(),
          Config.getSchemaPath(SysObject.FORM.getSchemaName()));

      if (doc != null) {
        Element formElement = doc.getDocumentElement();
        String viewName = formElement.getAttribute(UiConstants.ATTR_VIEW_NAME);

        if (BeeUtils.isEmpty(viewName)) {
          viewName = formElement.getAttribute(UiConstants.ATTR_DATA);
        }
        formInfo.setViewName(viewName);
      }
    }
    return formInfo.getViewName();
  }

  private String getGridViewName(String gridName) {
    if (!isGrid(gridName)) {
      return null;
    }
    UiObjectInfo gridInfo = gridCache.get(key(gridName));

    if (!gridInfo.isViewSet()) {
      Document doc = XmlUtils.getXmlResource(gridInfo.getResource(),
          Config.getSchemaPath(SysObject.GRID.getSchemaName()));

      if (doc != null) {
        gridInfo.setViewName(doc.getDocumentElement().getAttribute(UiConstants.ATTR_VIEW_NAME));
      }
    }
    return gridInfo.getViewName();
  }

  private Set<String> getHiddenColumns() {
    Set<String> result = new HashSet<>();

    Integer count = prm.getInteger(Dimensions.PRM_DIMENSIONS);
    if (count != null && !Objects.equals(count, Dimensions.getObserved())) {
      Dimensions.setObserved(count);
    }

    result.addAll(Dimensions.getHiddenRelationColumns());

    return result;
  }

  private Menu getMenu(String parent, Menu entry, boolean checkRights, boolean transform) {
    boolean visible;
    String ref = RightsUtils.NAME_JOINER.join(parent, entry.getName());

    if (checkRights) {
      visible = usr.isMenuVisible(ref);

      if (visible && !BeeUtils.isEmpty(entry.getModule())) {
        visible = usr.isAnyModuleVisible(entry.getModule());
      }
      if (visible && !BeeUtils.isEmpty(entry.getData())) {
        visible = usr.isDataVisible(entry.getData());
      }
      if (visible && entry instanceof MenuItem) {
        visible = hasMenuDataRights((MenuItem) entry);
      }
    } else {
      visible = Module.isAnyEnabled(entry.getModule());
    }

    if (visible) {
      if (entry instanceof MenuEntry) {
        List<Menu> input = ((MenuEntry) entry).getItems();

        if (!BeeUtils.isEmpty(input)) {
          List<Menu> output = new ArrayList<>();

          for (Menu item : input) {
            if (getMenu(ref, item, checkRights, transform) != null) {
              if (transform) {
                List<Menu> list = maybeTransform(item);
                if (list != null) {
                  output.addAll(list);
                }

              } else {
                output.add(item);
              }
            }
          }

          ((MenuEntry) entry).setItems(output);
        }
      }
      return entry;

    } else {
      return null;
    }
  }

  private boolean hasMenuDataRights(MenuItem item) {
    MenuService service = item.getService();

    if (service != null) {
      String parameters = item.getParameters();

      Set<String> dataNames = service.getDataNames(parameters);
      Set<RightsState> states = service.getDataRightsStates();

      if (!BeeUtils.isEmpty(dataNames) && !BeeUtils.isEmpty(states)) {
        for (String dataName : dataNames) {
          for (RightsState state : states) {
            if (!usr.hasDataRight(dataName, state)) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  private void initObjects(SysObject obj) {
    Assert.notNull(obj);

    switch (obj) {
      case GRID:
        gridCache.clear();
        break;
      case FORM:
        formCache.clear();
        break;
      case MENU:
        menuCache.clear();
        break;
      case REPORT:
        reportCache.clear();
        break;
      default:
        Assert.unsupported("Not an UI object: " + obj.getName());
    }
    int cnt = 0;
    String ext = "." + obj.getFileExtension();

    for (String moduleName : moduleBean.getModules()) {
      List<File> resources = FileUtils.findFiles("*" + ext,
          Collections.singleton(new File(Config.CONFIG_DIR,
              moduleBean.getResourcePath(moduleName, obj.getPath()))), null, null, false, true);

      if (!BeeUtils.isEmpty(resources)) {
        for (File resource : resources) {
          String resourcePath = resource.getPath();
          String objectName = BeeUtils.removeSuffix(FileNameUtils.getName(resourcePath), ext);
          cnt += initUiObject(obj, moduleName, objectName, resourcePath, true) ? 1 : 0;
        }
      }
    }
    Multimap<String, Pair<String, String>> custom = HashMultimap.create();

    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_CUSTOM_CONFIG, COL_CONFIG_MODULE, COL_CONFIG_OBJECT, COL_CONFIG_DATA)
        .addFrom(TBL_CUSTOM_CONFIG)
        .setWhere(SqlUtils.equals(TBL_CUSTOM_CONFIG, COL_CONFIG_TYPE, obj)));

    for (SimpleRow row : rs) {
      Module module = EnumUtils.getEnumByIndex(Module.class, row.getInt(COL_CONFIG_MODULE));

      if (Objects.nonNull(module)) {
        custom.put(module.getName(),
            Pair.of(row.getValue(COL_CONFIG_OBJECT), row.getValue(COL_CONFIG_DATA)));
      }
    }
    for (String moduleName : moduleBean.getModules()) {
      for (Pair<String, String> pair : custom.get(moduleName)) {
        cnt += initUiObject(obj, moduleName, pair.getA(), pair.getB(), false) ? 1 : 0;
      }
    }
    if (cnt <= 0) {
      logger.severe("No", obj.getName(), "descriptions found");
    } else {
      logger.info("Loaded", cnt, obj.getName(), "descriptions");
    }
  }

  private boolean initUiObject(SysObject obj, String moduleName, String objectName,
      String resource, boolean initial) {

    switch (obj) {
      case GRID:
        UiObjectInfo grid = new UiObjectInfo(moduleName, objectName, resource);
        return SysObject.register(grid, gridCache, initial, logger);
      case FORM:
        UiObjectInfo form = new UiObjectInfo(moduleName, objectName, resource);
        return SysObject.register(form, formCache, initial, logger);
      case MENU:
        Menu menu;

        try {
          menu = XmlUtils.unmarshal(Menu.class, resource,
              Config.getSchemaPath(obj.getSchemaName()));
        } catch (Throwable e) {
          logger.error(e);
          menu = null;
        }
        if (menu != null) {
          if (!BeeUtils.same(menu.getName(), objectName)) {
            logger.warning("Menu name doesn't match resource name:", menu.getName(), "!=",
                objectName);
            menu = null;

          } else if (BeeUtils.isEmpty(menu.getModule())) {
            menu.setModuleName(moduleName);
          }
        }
        return SysObject.register(menu, menuCache, initial, logger);
      case REPORT:
        UiObjectInfo report = new UiObjectInfo(moduleName, objectName, resource);
        return SysObject.register(report, reportCache, initial, logger);
      default:
        return false;
    }
  }

  private boolean isSourceVisible(BeeView view, String source, Set<String> hiddenColumns) {
    return usr.isColumnVisible(view, source) && !BeeUtils.contains(hiddenColumns, source);
  }

  private boolean isListVisible(BeeView view, String listName) {
    return view != null && usr.isListVisible(view.getName(), listName);
  }

  private boolean isWidgetVisible(Element element, BeeView view, Set<String> hiddenColumns) {
    if (element.hasAttribute(UiConstants.ATTR_MODULE)
        && !usr.isAnyModuleVisible(element.getAttribute(UiConstants.ATTR_MODULE))) {
      return false;
    }

    if (view != null) {
      String source = element.getAttribute(UiConstants.ATTR_SOURCE);
      if (!BeeUtils.isEmpty(source) && !isSourceVisible(view, source, hiddenColumns)) {
        return false;
      }

      String listName = element.getAttribute(UiConstants.ATTR_LIST_NAME);
      if (!BeeUtils.isEmpty(listName) && !isListVisible(view, listName)) {
        return false;
      }
    }

    String data = element.getAttribute(UiConstants.ATTR_DATA);
    String field = element.getAttribute(UiConstants.ATTR_FOR);

    if (BeeUtils.isEmpty(data)) {
      if (view != null && !BeeUtils.isEmpty(field)
          && !isSourceVisible(view, field, hiddenColumns)) {
        return false;
      }

    } else {
      if (!usr.isDataVisible(data)) {
        return false;
      }

      if (!BeeUtils.isEmpty(field) && !isSourceVisible(sys.getView(data), field, hiddenColumns)) {
        return false;
      }
    }

    String name = XmlUtils.getLocalName(element);

    if (isEmbeddedGrid(name)) {
      String gridName = BeeUtils.notEmpty(element.getAttribute(UiConstants.ATTR_GRID_NAME),
          element.getAttribute(UiConstants.ATTR_NAME));
      String gridViewName = BeeUtils.isEmpty(gridName) ? null : getGridViewName(gridName);

      if (!BeeUtils.isEmpty(gridViewName) && !usr.isDataVisible(gridViewName)) {
        return false;
      }

    } else if (BeeUtils.same(name, UiConstants.TAG_DATA_TREE)) {
      String widgetViewName = element.getAttribute(UiConstants.ATTR_VIEW_NAME);

      if (!BeeUtils.isEmpty(widgetViewName) && !usr.isDataVisible(widgetViewName)) {
        return false;
      }

    } else if (BeeUtils.same(name, UiConstants.TAG_CHILD_SELECTOR)) {
      if (view != null && BeeUtils.isEmpty(element.getAttribute(UiConstants.ATTR_LIST_NAME))) {
        String listName = element.getAttribute(UiConstants.ATTR_CHILD_TABLE);

        if (!BeeUtils.isEmpty(listName) && !isListVisible(view, listName)) {
          return false;
        }
      }
    }

    return true;
  }
}
