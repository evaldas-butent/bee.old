package com.butent.webservice;

import com.google.common.collect.Lists;

import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class WSDocument {

  public static final class WSDocumentItem {
    private final String itemId;
    private final String quantity;
    private String price;
    private String vatPercent;

    private WSDocumentItem(String itemId, String quantity) {
      this.itemId = itemId;
      this.quantity = quantity;
    }

    public void setPrice(String price) {
      this.price = price;
    }

    public void setVatPercent(String vatPercent) {
      this.vatPercent = vatPercent;
    }
  }

  private final String documentId;
  private final DateTime date;
  private final String operation;
  private final String company;
  private String companyCode;
  private String companyVATCode;
  private final String warehouse;

  private final List<WSDocumentItem> items = Lists.newArrayList();

  public WSDocument(String documentId, DateTime date, String operation, String company,
      String warehouse) {
    this.documentId = documentId;
    this.date = date;
    this.operation = operation;
    this.company = company;
    this.warehouse = warehouse;
  }

  public WSDocumentItem addItem(String itemId, String quantity) {
    WSDocumentItem item = new WSDocumentItem(itemId, quantity);
    items.add(item);
    return item;
  }

  public String getXml() {
    StringBuilder sb = new StringBuilder("<VFPData>");

    for (WSDocumentItem item : items) {
      sb.append("<row>")
          .append("<apyv_id>").append(documentId).append("</apyv_id>")
          .append("<data>").append(date.toString()).append("</data>")
          .append("<operacija>").append(operation).append("</operacija>")
          .append("<klientas>").append(company).append("</klientas>")
          .append("<sandelis>").append(warehouse).append("</sandelis>")
          .append("<preke>").append(item.itemId).append("</preke>")
          .append("<kiekis>").append(item.quantity).append("</kiekis>");

      if (!BeeUtils.isEmpty(companyCode)) {
        sb.append("<kodas>").append(companyCode).append("</kodas>");
      }
      if (!BeeUtils.isEmpty(companyVATCode)) {
        sb.append("<pvm_kodas>").append(companyVATCode).append("</pvm_kodas>");
      }
      if (!BeeUtils.isEmpty(item.price)) {
        sb.append("<kaina>").append(item.price).append("</kaina>");

        if (!BeeUtils.isEmpty(item.vatPercent)) {
          sb.append("<pvm>").append(item.vatPercent).append("</pvm>");
        }
      }
      sb.append("</row>");
    }
    return sb.append("</VFPData>").toString();
  }

  public void setCompanyCode(String companyCode) {
    this.companyCode = companyCode;
  }

  public void setCompanyVATCode(String companyVATCode) {
    this.companyVATCode = companyVATCode;
  }
}
