package com.butent.bee.egg.shared.exceptions;

@SuppressWarnings("serial")
public class ArgumentTypeException extends BeeRuntimeException {
  private String argType = null;
  private String reqType = null;

  public ArgumentTypeException() {
    super();
  }

  public ArgumentTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public ArgumentTypeException(String message) {
    super(message);
  }

  public ArgumentTypeException(Throwable cause) {
    super(cause);
  }

  public ArgumentTypeException(String argType, String reqType) {
    this();
    this.argType = argType;
    this.reqType = reqType;
  }

  public ArgumentTypeException(String argType, String reqType, String message) {
    this(message);
    this.argType = argType;
    this.reqType = reqType;
  }

  public String getArgType() {
    return argType;
  }

  public void setArgType(String argType) {
    this.argType = argType;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

}
