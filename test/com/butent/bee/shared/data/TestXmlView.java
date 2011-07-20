package com.butent.bee.shared.data;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.SystemBean.SysObject;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.junit.Test;

public class TestXmlView {
  @Test
  public void testRead() {
    String resource = Config.getPath(SysObject.VIEW.getFileName("Nationalities"), true);
    String schemaSource = Config.getSchemaPath(SysObject.VIEW.getSchema());

    if (!BeeUtils.isEmpty(resource)) {
      XmlView view = XmlUtils.unmarshal(XmlView.class, resource, schemaSource);
      System.out.println(XmlUtils.marshal(view, schemaSource));
    }
  }
}
