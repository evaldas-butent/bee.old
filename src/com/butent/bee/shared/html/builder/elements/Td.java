package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Td extends FertileNode {

  public Td() {
    super("td");
  }

  public Td insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Td append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Td append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Td text(String text) {
    super.appendText(text);
    return this;
  }

  public Td remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Td setAbbr(String value) {
    setAttribute("abbr", value);
    return this;
  }

  public String getAbbr() {
    return getAttribute("abbr");
  }

  public boolean removeAbbr() {
    return removeAttribute("abbr");
  }

  public Td setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Td setAxis(String value) {
    setAttribute("axis", value);
    return this;
  }

  public String getAxis() {
    return getAttribute("axis");
  }

  public boolean removeAxis() {
    return removeAttribute("axis");
  }

  public Td setBgcolor(String value) {
    setAttribute("bgcolor", value);
    return this;
  }

  public String getBgcolor() {
    return getAttribute("bgcolor");
  }

  public boolean removeBgcolor() {
    return removeAttribute("bgcolor");
  }

  public Td setChar(String value) {
    setAttribute("char", value);
    return this;
  }

  public String getChar() {
    return getAttribute("char");
  }

  public boolean removeChar() {
    return removeAttribute("char");
  }

  public Td setCharoff(String value) {
    setAttribute("charoff", value);
    return this;
  }

  public String getCharoff() {
    return getAttribute("charoff");
  }

  public boolean removeCharoff() {
    return removeAttribute("charoff");
  }

  public Td setColspan(String value) {
    setAttribute("colspan", value);
    return this;
  }

  public String getColspan() {
    return getAttribute("colspan");
  }

  public boolean removeColspan() {
    return removeAttribute("colspan");
  }

  public Td setHeaders(String value) {
    setAttribute("headers", value);
    return this;
  }

  public String getHeaders() {
    return getAttribute("headers");
  }

  public boolean removeHeaders() {
    return removeAttribute("headers");
  }

  public Td setHeight(String value) {
    setAttribute("height", value);
    return this;
  }

  public String getHeight() {
    return getAttribute("height");
  }

  public boolean removeHeight() {
    return removeAttribute("height");
  }

  public Td setNowrap(String value) {
    setAttribute("nowrap", value);
    return this;
  }

  public String getNowrap() {
    return getAttribute("nowrap");
  }

  public boolean removeNowrap() {
    return removeAttribute("nowrap");
  }

  public Td setRowspan(String value) {
    setAttribute("rowspan", value);
    return this;
  }

  public String getRowspan() {
    return getAttribute("rowspan");
  }

  public boolean removeRowspan() {
    return removeAttribute("rowspan");
  }

  public Td setScope(String value) {
    setAttribute("scope", value);
    return this;
  }

  public String getScope() {
    return getAttribute("scope");
  }

  public boolean removeScope() {
    return removeAttribute("scope");
  }

  public Td setValign(String value) {
    setAttribute("valign", value);
    return this;
  }

  public String getValign() {
    return getAttribute("valign");
  }

  public boolean removeValign() {
    return removeAttribute("valign");
  }

  public Td setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Td setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Td setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Td setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Td setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Td setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Td setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Td setXMLLang(String value) {
    setAttribute("xml:lang", value);
    return this;
  }

  public String getXMLLang() {
    return getAttribute("xml:lang");
  }

  public boolean removeXMLLang() {
    return removeAttribute("xml:lang");
  }

}
