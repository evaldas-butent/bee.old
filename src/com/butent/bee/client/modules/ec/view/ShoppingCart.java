package com.butent.bee.client.modules.ec.view;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.modules.ec.EcKeeper;
import com.butent.bee.client.modules.ec.EcStyles;
import com.butent.bee.client.modules.ec.EcUtils;
import com.butent.bee.client.modules.ec.widget.ItemPicture;
import com.butent.bee.client.widget.BeeListBox;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.ec.Cart;
import com.butent.bee.shared.modules.ec.CartItem;
import com.butent.bee.shared.modules.ec.DeliveryMethod;
import com.butent.bee.shared.modules.ec.EcConstants;
import com.butent.bee.shared.modules.ec.EcConstants.CartType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ShoppingCart extends Split {

  private static final String STYLE_PRIMARY = EcStyles.name("shoppingCart");
  private static final String STYLE_ITEMS = STYLE_PRIMARY + "-items";
  private static final String STYLE_ITEM = STYLE_PRIMARY + "-item";

  private static final String STYLE_HEADER_ROW = STYLE_ITEMS + "-headerRow";
  private static final String STYLE_ITEM_ROW = STYLE_PRIMARY + "-itemRow";

  private static final String STYLE_DELIVERY_ADDRESS = STYLE_PRIMARY + "-address";
  private static final String STYLE_DELIVERY_METHOD = STYLE_PRIMARY + "-method";
  private static final String STYLE_COMMENT = STYLE_PRIMARY + "-comment";

  private static final String STYLE_PICTURE = STYLE_ITEM + "-picture";
  private static final String STYLE_NAME = STYLE_ITEM + "-name";
  private static final String STYLE_CODE = STYLE_ITEM + "-code";
  private static final String STYLE_BRAND = STYLE_ITEM + "-brand";
  private static final String STYLE_QUANTITY = STYLE_ITEM + "-quantity";
  private static final String STYLE_PRICE = STYLE_ITEM + "-price";
  private static final String STYLE_REMOVE = STYLE_ITEM + "-remove";

  private static final String STYLE_PANEL = "-panel";
  private static final String STYLE_LABEL = "-label";
  private static final String STYLE_INPUT = "-input";

  private static final int COL_PICTURE = 0;
  private static final int COL_NAME = 1;
  private static final int COL_CODE = 2;
  private static final int COL_BRAND = 3;
  private static final int COL_QUANTITY = 4;
  private static final int COL_PRICE = 5;
  private static final int COL_REMOVE = 6;

  private static final int SIZE_NORTH = 32;
  private static final int SIZE_SOUTH = 180;
  private static final int MARGIN_SOUTH = 25;

  private final CartType cartType;
  private final List<DeliveryMethod> deliveryMethods;

  private final HtmlTable itemTable = new HtmlTable(STYLE_ITEMS + "-table");
  private final CustomDiv totalWidget = new CustomDiv(STYLE_PRIMARY + "-total");

  public ShoppingCart(CartType cartType, Cart cart, List<DeliveryMethod> deliveryMethods) {
    super(0);
    addStyleName(STYLE_PRIMARY);

    this.cartType = cartType;
    this.deliveryMethods = deliveryMethods;

    initNorth();
    initSouth(cart);
    initCenter();

    renderItems(cart.getItems());
  }

  @Override
  protected void onLoad() {
    super.onLoad();

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        int containerHeight = BeeUtils.positive(getOffsetHeight(),
            BeeKeeper.getScreen().getActivePanelHeight());
        int itemsHeight = itemTable.getOffsetHeight();

        if (itemsHeight > 0 && containerHeight > itemsHeight) {
          int southHeight = containerHeight - SIZE_NORTH - itemsHeight - MARGIN_SOUTH;
          if (southHeight > SIZE_SOUTH) {
            setDirectionSize(Direction.SOUTH, southHeight, true);
          }
        }
      }
    });
  }

  private void doSubmit() {
    Cart cart = EcKeeper.getCart(cartType);
    if (cart == null || cart.isEmpty()) {
      return;
    }

    if (cart.getDeliveryMethod() == null) {
      BeeKeeper.getScreen().notifyWarning(Localized.getConstants().ecDeliveryMethodRequired());
      return;
    }

    final String amount = EcUtils.renderCents(cart.totalCents());

    ParameterList params = EcKeeper.createArgs(EcConstants.SVC_SUBMIT_ORDER);
    params.addQueryItem(EcConstants.COL_SHOPPING_CART_TYPE, cartType.ordinal());
    params.addDataItem(EcConstants.VAR_CART, cart.serialize());

    BeeKeeper.getRpc().makePostRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        EcKeeper.dispatchMessages(response);

        if (response.hasResponse(Long.class)) {
          EcKeeper.resetCart(cartType);
          EcKeeper.closeView(ShoppingCart.this);

          Global.showInfo(Localized.getConstants().ecOrderSubmitted(),
              Lists.newArrayList(Localized.getMessages().ecOrderId(response.getResponseAsString()),
                  Localized.getMessages().ecOrderTotal(amount, EcConstants.CURRENCY)));
        }
      }
    });
  }

  private static int getInt(HasText widget) {
    return BeeUtils.toInt(widget.getText());
  }

  private void initCenter() {
    Simple wrapper = new Simple(itemTable);
    wrapper.addStyleName(STYLE_ITEMS + "-wrapper");

    add(wrapper);
  }

  private void initNorth() {
    Label caption = new Label(cartType.getCaption());
    caption.addStyleName(STYLE_PRIMARY + "-caption");

    addNorth(caption, SIZE_NORTH);
  }

  private void initSouth(Cart cart) {
    Flow panel = new Flow(STYLE_PRIMARY + "-south");

    totalWidget.setHTML(renderTotal(cart));
    panel.add(totalWidget);

    if (!BeeUtils.isEmpty(deliveryMethods)) {
      Widget addressWidget = renderDeliveryAddress(cart);
      if (addressWidget != null) {
        panel.add(addressWidget);
      }

      Widget methodWidget = renderDeliveryMethod(cart);
      if (methodWidget != null) {
        panel.add(methodWidget);
      }

      Widget commentWidget = renderComment(cart);
      if (commentWidget != null) {
        panel.add(commentWidget);
      }

      Button submitWidget = new Button(Localized.getConstants().ecShoppingCartSubmit());
      submitWidget.addStyleName(STYLE_PRIMARY + "-submit");
      submitWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          doSubmit();
        }
      });
      panel.add(submitWidget);
    }

    addSouth(panel, SIZE_SOUTH);
  }

  private static Widget renderBrand(CartItem item) {
    Long brand = item.getEcItem().getBrand();
    return (brand == null) ? null : new Label(EcKeeper.getBrandName(brand));
  }

  private static Widget renderComment(final Cart cart) {
    Flow panel = new Flow(STYLE_COMMENT + STYLE_PANEL);

    Label label = new Label(Localized.getConstants().comment());
    label.addStyleName(STYLE_COMMENT + STYLE_LABEL);
    panel.add(label);

    final InputArea input = new InputArea();
    input.addStyleName(STYLE_COMMENT + STYLE_INPUT);

    if (!BeeUtils.isEmpty(cart.getComment())) {
      input.setValue(BeeUtils.trim(cart.getComment()));
    }
    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        cart.setComment(Strings.emptyToNull(BeeUtils.trim(input.getValue())));
      }
    });

    panel.add(input);

    return panel;
  }

  private static Widget renderDeliveryAddress(final Cart cart) {
    Flow panel = new Flow(STYLE_DELIVERY_ADDRESS + STYLE_PANEL);

    Label label = new Label(Localized.getConstants().ecDeliveryAddress());
    label.addStyleName(STYLE_DELIVERY_ADDRESS + STYLE_LABEL);
    panel.add(label);

    final InputArea input = new InputArea();
    input.addStyleName(STYLE_DELIVERY_ADDRESS + STYLE_INPUT);

    if (!BeeUtils.isEmpty(cart.getDeliveryAddress())) {
      input.setValue(BeeUtils.trim(cart.getDeliveryAddress()));
    }
    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        cart.setDeliveryAddress(Strings.emptyToNull(BeeUtils.trim(input.getValue())));
      }
    });

    panel.add(input);

    return panel;
  }

  private Widget renderDeliveryMethod(final Cart cart) {
    Flow panel = new Flow(STYLE_DELIVERY_METHOD + STYLE_PANEL);

    Label label = new Label(Localized.getConstants().ecDeliveryMethod());
    label.addStyleName(STYLE_DELIVERY_METHOD + STYLE_LABEL);
    panel.add(label);

    final BeeListBox input = new BeeListBox();
    input.addStyleName(STYLE_DELIVERY_METHOD + STYLE_INPUT);

    for (DeliveryMethod deliveryMethod : deliveryMethods) {
      input.addItem(deliveryMethod.getName());
    }

    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        input.deselect();

        if (cart.getDeliveryMethod() != null) {
          for (int i = 0; i < deliveryMethods.size(); i++) {
            if (cart.getDeliveryMethod().equals(deliveryMethods.get(i).getId())) {
              input.setSelectedIndex(i);
              break;
            }
          }
        }
      }
    });

    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        int index = input.getSelectedIndex();
        cart.setDeliveryMethod(BeeUtils.isIndex(deliveryMethods, index)
            ? deliveryMethods.get(index).getId() : null);
      }
    });

    panel.add(input);

    return panel;
  }

  private static Widget renderCode(CartItem item) {
    return new Label(item.getEcItem().getCode());
  }

  private void renderItem(int row, CartItem item, Widget pictureWidget) {
    if (pictureWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_PICTURE, pictureWidget, STYLE_PICTURE);
    }

    Widget nameWidget = renderName(item);
    if (nameWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_NAME, nameWidget, STYLE_NAME);
    }

    Widget codeWidget = renderCode(item);
    if (codeWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_CODE, codeWidget, STYLE_CODE);
    }

    Widget brandWidget = renderBrand(item);
    if (brandWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_BRAND, brandWidget, STYLE_BRAND);
    }

    Widget qtyWidget = renderQuantity(item);
    if (qtyWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_QUANTITY, qtyWidget, STYLE_QUANTITY);
    }

    Widget priceWidget = renderPrice(item);
    if (priceWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_PRICE, priceWidget, STYLE_PRICE);
    }

    Widget removeWidget = renderRemove(item);
    if (removeWidget != null) {
      itemTable.setWidgetAndStyle(row, COL_REMOVE, removeWidget, STYLE_REMOVE);
    }

    itemTable.getRowFormatter().addStyleName(row, STYLE_ITEM_ROW);
  }

  private void renderItems(List<CartItem> items) {
    if (!itemTable.isEmpty()) {
      itemTable.clear();
    }

    if (!BeeUtils.isEmpty(items)) {
      int row = 0;

      Label nameLabel = new Label(Localized.getConstants().ecItemName());
      nameLabel.addStyleName(STYLE_NAME + STYLE_LABEL);
      itemTable.setWidget(row, COL_NAME, nameLabel);

      Label codeLabel = new Label(Localized.getConstants().ecItemCode());
      codeLabel.addStyleName(STYLE_CODE + STYLE_LABEL);
      itemTable.setWidget(row, COL_CODE, codeLabel);

      Label brandLabel = new Label(Localized.getConstants().ecItemBrand());
      brandLabel.addStyleName(STYLE_BRAND + STYLE_LABEL);
      itemTable.setWidget(row, COL_BRAND, brandLabel);

      Label qtyLabel = new Label(Localized.getConstants().quantity());
      qtyLabel.addStyleName(STYLE_QUANTITY + STYLE_LABEL);
      itemTable.setWidget(row, COL_QUANTITY, qtyLabel);

      Label priceLabel = new Label(Localized.getConstants().price());
      priceLabel.addStyleName(STYLE_PRICE + STYLE_LABEL);
      itemTable.setWidget(row, COL_PRICE, priceLabel);

      Label removeLabel = new Label(Localized.getConstants().ecShoppingCartRemove());
      removeLabel.addStyleName(STYLE_REMOVE + STYLE_LABEL);
      itemTable.setWidget(row, COL_REMOVE, removeLabel);

      itemTable.getRowFormatter().addStyleName(row, STYLE_HEADER_ROW);

      Multimap<Long, ItemPicture> pictureWidgets = ArrayListMultimap.create();

      row++;
      for (CartItem item : items) {
        ItemPicture pictureWidget = new ItemPicture();
        renderItem(row++, item, pictureWidget);

        pictureWidgets.put(item.getEcItem().getArticleId(), pictureWidget);
      }

      if (!pictureWidgets.isEmpty()) {
        EcKeeper.setBackgroundPictures(pictureWidgets);
      }
    }
  }

  private static Widget renderName(final CartItem item) {
    Label nameWidget = new Label(item.getEcItem().getName());

    nameWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EcKeeper.openItem(item.getEcItem(), false);
      }
    });

    return nameWidget;
  }

  private static Widget renderPrice(CartItem item) {
    return new Label(EcUtils.renderCents(item.getEcItem().getPrice()));
  }

  private Widget renderQuantity(final CartItem item) {
    String stylePrefix = STYLE_QUANTITY + "-";

    Horizontal panel = new Horizontal();

    final CustomDiv valueWidget = new CustomDiv(stylePrefix + "value");
    setInt(valueWidget, item.getQuantity());

    panel.add(valueWidget);

    Flow spin = new Flow(stylePrefix + "spin");

    Image plus = new Image(Global.getImages().silverPlus());
    plus.addStyleName(stylePrefix + "plus");

    plus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = getInt(valueWidget) + 1;
        setInt(valueWidget, value);

        item.setQuantity(value);

        BeeKeeper.getScreen().notifyInfo(Localized.getMessages()
            .ecUpdateCartItem(cartType.getCaption(), item.getEcItem().getName(), value));

        Cart cart = EcKeeper.refreshCart(cartType);
        updateTotal(cart);

        EcKeeper.persistCartItem(cartType, item);
      }
    });
    spin.add(plus);

    Image minus = new Image(Global.getImages().silverMinus());
    minus.addStyleName(stylePrefix + "minus");

    minus.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        int value = getInt(valueWidget) - 1;
        if (value > 0) {
          setInt(valueWidget, value);

          item.setQuantity(value);

          BeeKeeper.getScreen().notifyInfo(Localized.getMessages()
              .ecUpdateCartItem(cartType.getCaption(), item.getEcItem().getName(), value));

          Cart cart = EcKeeper.refreshCart(cartType);
          updateTotal(cart);

          EcKeeper.persistCartItem(cartType, item);
        }
      }
    });
    spin.add(minus);

    panel.add(spin);

    return panel;
  }

  private Widget renderRemove(final CartItem item) {
    Image remove = new Image(EcUtils.imageUrl("shoppingcart_remove.png"));
    remove.setAlt("remove");

    remove.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Cart cart = EcKeeper.removeFromCart(cartType, item.getEcItem());
        if (cart != null) {
          if (cart.isEmpty()) {
            EcKeeper.closeView(ShoppingCart.this);
          } else {
            renderItems(cart.getItems());
            updateTotal(cart);
          }
        }
      }
    });

    return remove;
  }

  private static String renderTotal(Cart cart) {
    return BeeUtils.joinWords(Localized.getConstants().ecShoppingCartTotal(),
        EcUtils.renderCents(cart.totalCents()), EcConstants.CURRENCY);
  }

  private static void setInt(HasText widget, int value) {
    widget.setText(BeeUtils.toString(value));
  }

  private void updateTotal(Cart cart) {
    if (cart != null) {
      totalWidget.setHTML(renderTotal(cart));
    }
  }
}
