package com.butent.bee.client.dialog;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.CellGrid;
import com.butent.bee.client.grid.CellType;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.tree.BeeTree;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Iterator;

public class MessageBox {

  public void alert(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    Window.alert(BeeUtils.concat(BeeConst.CHAR_EOL, obj));
  }

  public boolean close(Object src) {
    boolean ok = false;

    if (src instanceof Widget) {
      PopupPanel p = DomUtils.parentPopup((Widget) src);
      if (p != null) {
        p.hide();
        ok = true;
      }
    }
    return ok;
  }

  public boolean confirm(Object... obj) {
    Assert.notNull(obj);    
    Assert.parameterCount(obj.length, 1);
    return Window.confirm(BeeUtils.concat(BeeConst.CHAR_EOL, obj));
  }

  public void showError(Object... x) {
    showInfo(x);
  }
  
  public void showGrid(String cap, Object data, String... columnLabels) {
    Assert.notNull(data);
    showInfo(cap, Global.cellGrid(data, CellType.TEXT, columnLabels));
  }

  public void showInfo(Object... x) {
    Assert.notNull(x);
    int n = x.length;
    Assert.parameterCount(n, 1);

    Vertical vp = new Vertical();

    for (int i = 0; i < n; i++) {
      if (x[i] instanceof Widget) {
        vp.add((Widget) x[i]);

        if (x[i] instanceof CellGrid) {
          vp.setCellHeight((Widget) x[i], "200px");
          vp.setCellWidth((Widget) x[i], "400px");
        } else if (x[i] instanceof BeeTree) {
          vp.setCellHeight((Widget) x[i], "500px");
          vp.setCellWidth((Widget) x[i], "400px");
        }

      } else if (x[i] instanceof String) {
        vp.add(new BeeLabel((String) x[i]));
      } else if (x[i] instanceof Collection) {
        for (Iterator<?> iter = ((Collection<?>) x[i]).iterator(); iter.hasNext();) {
          vp.add(new BeeLabel(iter.next()));
        }
      } else if (ArrayUtils.isArray(x[i])) {
        for (int j = 0; j < ArrayUtils.length(x[i]); j++) {
          vp.add(new BeeLabel(ArrayUtils.get(x[i], j)));
        }
      } else if (x[i] != null) {
        vp.add(new BeeLabel(x[i]));
      }
    }

    CloseButton b = new CloseButton("ok");

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(b);

    BeePopupPanel box = new BeePopupPanel();
    box.setAnimationEnabled(true);

    box.setWidget(vp);

    box.center();
    b.setFocus(true);
  }

  public void showWidget(Widget widget) {
    Assert.notNull(widget);

    BeePopupPanel box = new BeePopupPanel();
    box.setAnimationEnabled(true);

    box.setWidget(widget);
    box.center();
  }
}
