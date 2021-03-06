package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.Attributes;
import com.butent.bee.shared.html.builder.Element;

public class Track extends Element {

  private static final String KIND_CAPTIONS = "captions";
  private static final String KIND_CHAPTERS = "chapters";
  private static final String KIND_DESCRIPTIONS = "descriptions";
  private static final String KIND_META_DATA = "metadata";
  private static final String KIND_SUBTITLES = "subtitles";

  public Track() {
    super();
  }

  public Track addClass(String value) {
    super.addClassName(value);
    return this;
  }

  public Track htmlDefault() {
    setAttribute(Attributes.DEFAULT, true);
    return this;
  }

  public Track id(String value) {
    setId(value);
    return this;
  }

  public Track kindCaptions() {
    setAttribute(Attributes.KIND, KIND_CAPTIONS);
    return this;
  }

  public Track kindChapters() {
    setAttribute(Attributes.KIND, KIND_CHAPTERS);
    return this;
  }

  public Track kindDescriptions() {
    setAttribute(Attributes.KIND, KIND_DESCRIPTIONS);
    return this;
  }

  public Track kindMetaData() {
    setAttribute(Attributes.KIND, KIND_META_DATA);
    return this;
  }

  public Track kindSubtitles() {
    setAttribute(Attributes.KIND, KIND_SUBTITLES);
    return this;
  }

  public Track label(String value) {
    setAttribute(Attributes.LABEL, value);
    return this;
  }

  public Track lang(String value) {
    setLang(value);
    return this;
  }

  public Track src(String value) {
    setAttribute(Attributes.SRC, value);
    return this;
  }

  public Track srcLang(String value) {
    setAttribute(Attributes.SRC_LANG, value);
    return this;
  }

  public Track title(String value) {
    setTitle(value);
    return this;
  }
}
