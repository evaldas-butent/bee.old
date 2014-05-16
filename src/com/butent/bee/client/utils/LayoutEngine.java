package com.butent.bee.client.utils;

import elemental.client.Browser.Info;
import elemental.js.JsBrowser;

public enum LayoutEngine {
  WEBKIT("webkit", "css/webkit.css?v=140314"),
  GECKO("gecko", null),
  TRIDENT("trident", "css/trident.css");
  
  public static LayoutEngine detect() {
    String userAgent = JsBrowser.getWindow().getNavigator().getUserAgent().toLowerCase();
    if (userAgent.contains(TRIDENT.substring)) {
      return TRIDENT;
    }

    Info info = JsBrowser.getInfo();
    
    if (info != null) {
      if (info.isWebKit()) {
        return WEBKIT;
      } else if (info.isGecko()) {
        return GECKO;
      }
    }
    
    if (BrowsingContext.isChrome()) {
      return WEBKIT;
    }
    
    for (LayoutEngine engine : LayoutEngine.values()) {
      if (userAgent.contains(engine.substring)) {
        return engine;
      }
    }
    return null;
  }
  
  private final String substring;
  private final String styleSheet;

  private LayoutEngine(String substring, String styleSheet) {
    this.substring = substring;
    this.styleSheet = styleSheet;
  }

  public String getStyleSheet() {
    return styleSheet;
  }
  
  public boolean hasStyleSheet() {
    return styleSheet != null;
  }
}
