package com.butent.bee.egg.server.http;

import com.butent.bee.egg.server.concurrency.Counter;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class RequestListener implements ServletRequestListener {
  private String attrCnt = null;

  public RequestListener() {
    super();
    attrCnt = HttpConst.ATTRIBUTE_REQUEST_COUNTER;
  }

  @Override
  public void requestDestroyed(ServletRequestEvent sre) {
    sre.getServletContext().log(
        "request destroyed "
            + HttpUtils.counterInfo(attrCnt,
                sre.getServletRequest().getAttribute(attrCnt)));
    sre.getServletRequest().removeAttribute(attrCnt);
  }

  @Override
  public void requestInitialized(ServletRequestEvent sre) {
    sre.getServletRequest().setAttribute(attrCnt, new Counter());
    sre.getServletContext().log("request initialized");
  }

}
