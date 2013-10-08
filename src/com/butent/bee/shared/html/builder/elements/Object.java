package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.FertileNode;
import com.butent.bee.shared.html.builder.Node;

import java.util.List;

public class Object extends FertileNode {

  public Object() {
    super("object");
  }

  public Object insert(int index, Node child) {
    super.insertChild(index, child);
    return this;
  }

  public Object append(List<Node> nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object append(Node... nodes) {
    super.appendChildren(nodes);
    return this;
  }

  public Object text(String text) {
    super.appendText(text);
    return this;
  }

  public Object remove(Node child) {
    super.removeChild(child);
    return this;
  }

  public Object setAlign(String value) {
    setAttribute("align", value);
    return this;
  }

  public String getAlign() {
    return getAttribute("align");
  }

  public boolean removeAlign() {
    return removeAttribute("align");
  }

  public Object setArchive(String value) {
    setAttribute("archive", value);
    return this;
  }

  public String getArchive() {
    return getAttribute("archive");
  }

  public boolean removeArchive() {
    return removeAttribute("archive");
  }

  public Object setBorder(String value) {
    setAttribute("border", value);
    return this;
  }

  public String getBorder() {
    return getAttribute("border");
  }

  public boolean removeBorder() {
    return removeAttribute("border");
  }

  public Object setClassid(String value) {
    setAttribute("classid", value);
    return this;
  }

  public String getClassid() {
    return getAttribute("classid");
  }

  public boolean removeClassid() {
    return removeAttribute("classid");
  }

  public Object setCodebase(String value) {
    setAttribute("codebase", value);
    return this;
  }

  public String getCodebase() {
    return getAttribute("codebase");
  }

  public boolean removeCodebase() {
    return removeAttribute("codebase");
  }

  public Object setCodetype(String value) {
    setAttribute("codetype", value);
    return this;
  }

  public String getCodetype() {
    return getAttribute("codetype");
  }

  public boolean removeCodetype() {
    return removeAttribute("codetype");
  }

  public Object setData(String value) {
    setAttribute("data", value);
    return this;
  }

  public String getData() {
    return getAttribute("data");
  }

  public boolean removeData() {
    return removeAttribute("data");
  }

  public Object setDeclare(String value) {
    setAttribute("declare", value);
    return this;
  }

  public String getDeclare() {
    return getAttribute("declare");
  }

  public boolean removeDeclare() {
    return removeAttribute("declare");
  }

  public Object setHeight(String value) {
    setAttribute("height", value);
    return this;
  }

  public String getHeight() {
    return getAttribute("height");
  }

  public boolean removeHeight() {
    return removeAttribute("height");
  }

  public Object setHspace(String value) {
    setAttribute("hspace", value);
    return this;
  }

  public String getHspace() {
    return getAttribute("hspace");
  }

  public boolean removeHspace() {
    return removeAttribute("hspace");
  }

  public Object setName(String value) {
    setAttribute("name", value);
    return this;
  }

  public String getName() {
    return getAttribute("name");
  }

  public boolean removeName() {
    return removeAttribute("name");
  }

  public Object setStandby(String value) {
    setAttribute("standby", value);
    return this;
  }

  public String getStandby() {
    return getAttribute("standby");
  }

  public boolean removeStandby() {
    return removeAttribute("standby");
  }

  public Object setType(String value) {
    setAttribute("type", value);
    return this;
  }

  public String getType() {
    return getAttribute("type");
  }

  public boolean removeType() {
    return removeAttribute("type");
  }

  public Object setUsemap(String value) {
    setAttribute("usemap", value);
    return this;
  }

  public String getUsemap() {
    return getAttribute("usemap");
  }

  public boolean removeUsemap() {
    return removeAttribute("usemap");
  }

  public Object setVspace(String value) {
    setAttribute("vspace", value);
    return this;
  }

  public String getVspace() {
    return getAttribute("vspace");
  }

  public boolean removeVspace() {
    return removeAttribute("vspace");
  }

  public Object setWidth(String value) {
    setAttribute("width", value);
    return this;
  }

  public String getWidth() {
    return getAttribute("width");
  }

  public boolean removeWidth() {
    return removeAttribute("width");
  }

  public Object setId(String value) {
    setAttribute("id", value);
    return this;
  }

  public String getId() {
    return getAttribute("id");
  }

  public boolean removeId() {
    return removeAttribute("id");
  }

  public Object setCSSClass(String value) {
    setAttribute("class", value);
    return this;
  }

  public String getCSSClass() {
    return getAttribute("class");
  }

  public boolean removeCSSClass() {
    return removeAttribute("class");
  }

  public Object setTitle(String value) {
    setAttribute("title", value);
    return this;
  }

  public String getTitle() {
    return getAttribute("title");
  }

  public boolean removeTitle() {
    return removeAttribute("title");
  }

  public Object setStyle(String value) {
    setAttribute("style", value);
    return this;
  }

  public String getStyle() {
    return getAttribute("style");
  }

  public boolean removeStyle() {
    return removeAttribute("style");
  }

  public Object setDir(String value) {
    setAttribute("dir", value);
    return this;
  }

  public String getDir() {
    return getAttribute("dir");
  }

  public boolean removeDir() {
    return removeAttribute("dir");
  }

  public Object setLang(String value) {
    setAttribute("lang", value);
    return this;
  }

  public String getLang() {
    return getAttribute("lang");
  }

  public boolean removeLang() {
    return removeAttribute("lang");
  }

  public Object setXMLLang(String value) {
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
