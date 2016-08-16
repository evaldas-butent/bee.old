package com.butent.bee.shared.modules.orders;

import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.modules.orders.SpecificationBuilder;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Specification implements BeeSerializable {

  private enum Serial {
    BRANCH_ID, BRANCH_OPTIONS, BUNDLE, BUNDLE_PRICE, OPTIONS, DESCRIPTION, ID
  }

  private Long branchId;
  private final List<Option> branchOptions = new ArrayList<>();
  private Bundle bundle;
  private Integer bundlePrice;
  private final Map<Option, Integer> options = new TreeMap<>();
  private String description;
  private Long id;

  public void addOption(Option option, Integer optionPrice) {
    options.put(option, optionPrice);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case BRANCH_ID:
          branchId = BeeUtils.toLongOrNull(value);
          break;

        case BRANCH_OPTIONS:
          branchOptions.clear();

          for (String data : Codec.beeDeserializeCollection(value)) {
            branchOptions.add(Option.restore(data));
          }
          break;

        case BUNDLE:
          bundle = Bundle.restore(value);
          break;

        case BUNDLE_PRICE:
          bundlePrice = BeeUtils.toIntOrNull(value);
          break;

        case OPTIONS:
          options.clear();

          for (Map.Entry<String, String> entry : Codec.deserializeMap(value).entrySet()) {
            options.put(Option.restore(entry.getKey()), BeeUtils.toIntOrNull(entry.getValue()));
          }
          break;

        case DESCRIPTION:
          description = value;
          break;

        case ID:
          id = BeeUtils.toLongOrNull(value);
          break;
      }
    }
  }

  public Long getBranchId() {
    return branchId;
  }

  public List<Option> getBranchOptions() {
    return branchOptions;
  }

  public Bundle getBundle() {
    return bundle;
  }

  public Integer getBundlePrice() {
    return bundlePrice;
  }

  public String getDescription() {
    return description;
  }

  public Long getId() {
    return id;
  }

  public Integer getOptionPrice(Option option) {
    return options.get(option);
  }

  public Set<Option> getOptions() {
    return options.keySet();
  }

  public int getPrice() {
    int price = BeeUtils.unbox(getBundlePrice());

    for (Integer optionPrice : options.values()) {
      price += BeeUtils.unbox(optionPrice);
    }
    return price;
  }

  public Widget renderSummary(boolean priceMode) {
    HtmlTable summary = new HtmlTable(SpecificationBuilder.STYLE_PREFIX + "-summary");
    int row = 0;
    List<String> branchNames = new ArrayList<>();

    for (Option option : getBranchOptions()) {
      branchNames.add(option.getName());
    }
    summary.setText(row, 0, BeeUtils.joinWords(branchNames));

    if (priceMode) {
      summary.setText(row, 1, BeeUtils.toString(getBundlePrice()));
    }
    for (Option option : getBundle().getOptions()) {
      row++;
      summary.setText(row, 0,
          BeeUtils.join(": ", option.getDimension().getName(), option.getName()));
    }
    int other = BeeConst.UNDEF;

    for (Option option : getOptions()) {
      Integer prc = options.get(option);

      if (option.getDimension().isRequired()) {
        row++;
        summary.setText(row, 0,
            BeeUtils.join(": ", option.getDimension().getName(), option.getName()));

        if (priceMode) {
          summary.setText(row, 1, BeeUtils.toString(prc));
        }
      } else {
        if (BeeConst.isUndef(other)) {
          other = 0;
        }
        other += BeeUtils.toInt(prc);
      }
    }
    if (priceMode && !BeeConst.isUndef(other)) {
      row++;
      summary.setText(row, 0, Localized.dictionary().otherCosts());
      summary.setText(row, 1, BeeUtils.toString(other));
    }
    if (priceMode) {
      int total = 0;

      for (TableCellElement cellElement : summary.getColumnCells(1)) {
        total += BeeUtils.toInt(cellElement.getInnerText());
      }
      row++;
      summary.setText(row, 0, Localized.dictionary().totalOf());
      summary.setText(row, 1, BeeUtils.toString(total));
    }
    return summary;
  }

  public static Specification restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Specification spec = new Specification();
    spec.deserialize(s);
    return spec;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case BRANCH_ID:
          arr[i++] = branchId;
          break;

        case BRANCH_OPTIONS:
          arr[i++] = branchOptions;
          break;

        case BUNDLE:
          arr[i++] = bundle;
          break;

        case BUNDLE_PRICE:
          arr[i++] = bundlePrice;
          break;

        case OPTIONS:
          arr[i++] = options;
          break;

        case DESCRIPTION:
          arr[i++] = description;
          break;

        case ID:
          arr[i++] = id;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setBranchOptions(Long branchId, List<Option> options) {
    this.branchId = branchId;
    branchOptions.clear();
    branchOptions.addAll(options);
  }

  public void setBundle(Bundle bundle, Integer price) {
    this.bundle = bundle;
    this.bundlePrice = price;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setId(Long id) {
    this.id = id;
  }
}