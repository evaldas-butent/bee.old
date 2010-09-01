package com.butent.bee.egg.server.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.butent.bee.egg.server.concurrency.Counter;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class HttpUtils {
  private static Logger logger = Logger.getLogger(HttpUtils.class.getName());

  public static Map<String, String> getParameters(HttpServletRequest req) {
    if (req == null)
      return null;

    Map<String, String[]> lst = req.getParameterMap();
    String nm;
    String[] v;

    if (BeeUtils.isEmpty(lst)) {
      Enumeration<String> z = req.getParameterNames();
      if (BeeUtils.isEmpty(z))
        return null;

      lst = new HashMap<String, String[]>();

      while (z.hasMoreElements()) {
        nm = z.nextElement();
        if (BeeUtils.isEmpty(nm))
          continue;

        v = req.getParameterValues(nm);
        if (BeeUtils.isEmpty(v))
          v = new String[] { req.getParameter(nm) };

        lst.put(nm, v);
      }
    }

    if (BeeUtils.isEmpty(lst))
      return null;

    Map<String, String> params = new HashMap<String, String>();

    for (Map.Entry<String, String[]> el : lst.entrySet()) {
      nm = el.getKey();
      v = el.getValue();
      if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v))
        continue;

      for (int i = 0; i < v.length; i++) {
        params.put(nm, v[i]);
      }
    }

    return params;
  }

  public static Map<String, String> getHeaders(HttpServletRequest req) {
    if (req == null)
      return null;

    Enumeration<String> lst = req.getHeaderNames();
    if (BeeUtils.isEmpty(lst))
      return null;

    Map<String, String> headers = new HashMap<String, String>();
    String nm, v;

    while (lst.hasMoreElements()) {
      nm = lst.nextElement();
      if (BeeUtils.isEmpty(nm))
        continue;

      v = BeeUtils.ifString(BeeUtils.transformEnumeration(req.getHeaders(nm)),
          req.getHeader(nm));
      if (BeeUtils.isEmpty(v))
        continue;

      headers.put(nm, v);
    }

    return headers;
  }

  public static String counterInfo(String name, Object obj) {
    if (obj instanceof Counter)
      return BeeUtils.addName(name, ((Counter) obj).transform());
    else
      return BeeConst.STRING_EMPTY;
  }

  public static void incrCounter(Object obj) {
    if (obj instanceof Counter)
      ((Counter) obj).incCounter();
  }

  public static String readContent(HttpServletRequest req) {
    Assert.notNull(req);

    StringBuffer sb = new StringBuffer();
    int cbSize = 2048;
    char[] cbuf = new char[cbSize];
    int len;

    try {
      BufferedReader reader = req.getReader();

      do {
        len = reader.read(cbuf, 0, cbSize);
        if (len > 0)
          sb.append(cbuf, 0, len);
        else
          break;
      } while (len > 0);

      reader.close();
    } catch (IOException ex) {
      logger.severe(ex.getMessage());
    }

    return sb.toString();
  }

}
