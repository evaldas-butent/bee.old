package com.butent.bee.client.dialog;

import com.butent.bee.client.Global;

public class DialogConstants {

  public static final String OK = Global.CONSTANTS.ok();
  public static final String CANCEL = Global.CONSTANTS.cancel();

  public static final String WIDGET_DIALOG = "dialog";
  public static final String WIDGET_PANEL = "panel";
  public static final String WIDGET_PROMPT = "prompt";
  public static final String WIDGET_INPUT = "input";
  public static final String WIDGET_ERROR = "error";
  public static final String WIDGET_COMMAND_GROUP = "commandGroup";
  public static final String WIDGET_COMMAND_ITEM = "commandItem";
  public static final String WIDGET_CONFIRM = "confirm";
  public static final String WIDGET_CANCEL = "cancel";

  public static final String WIDGET_PRINT = "print";
  public static final String WIDGET_SAVE = "save";
  public static final String WIDGET_CLOSE = "close";

  public static final String STYLE_REPORT_OPTIONS = "bee-ReportOptions";
  
  public static final int DECISION_YES = 0;
  public static final int DECISION_NO = 1;
  public static final int DECISION_CANCEL = 2;
  
  private DialogConstants() {
  }
}
