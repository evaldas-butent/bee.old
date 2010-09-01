package com.butent.bee.egg.server.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.butent.bee.egg.server.concurrency.Counter;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class RequestFilter implements Filter {
  public static Counter COUNTER = new Counter();
  private FilterConfig config = null;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    config = filterConfig;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    if (config == null)
      return;

    String rid = ((HttpServletRequest) request).getQueryString();

    if (!BeeUtils.isEmpty(rid)) {
      COUNTER.incCounter();

      ServletContext context = config.getServletContext();
      HttpSession session = ((HttpServletRequest) request).getSession();

      Object rc = request.getAttribute(HttpConst.ATTRIBUTE_REQUEST_COUNTER);
      Object cc = context.getAttribute(HttpConst.ATTRIBUTE_CONTEXT_COUNTER);
      Object sc = session.getAttribute(HttpConst.ATTRIBUTE_SESSION_COUNTER);

      HttpUtils.incrCounter(rc);
      HttpUtils.incrCounter(cc);
      HttpUtils.incrCounter(sc);

      context.log(BeeUtils.concat(1, "filter", System.nanoTime(),
          HttpUtils.counterInfo("counter", COUNTER),
          BeeUtils.addName("rid", rid),
          HttpUtils.counterInfo(HttpConst.ATTRIBUTE_REQUEST_COUNTER, rc),
          HttpUtils.counterInfo(HttpConst.ATTRIBUTE_CONTEXT_COUNTER, cc),
          HttpUtils.counterInfo(HttpConst.ATTRIBUTE_SESSION_COUNTER, sc)));
    }

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    config = null;
  }

}
