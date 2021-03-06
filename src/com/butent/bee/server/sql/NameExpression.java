package com.butent.bee.server.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Generates expressions for table and field names depending on specific SQL server requirements.
 */
class NameExpression implements IsExpression {

  private final String name;

  NameExpression(String name) {
    Assert.notEmpty(name);
    this.name = name;
  }

  @Override
  public String getSqlString(SqlBuilder builder) {
    StringBuilder s = new StringBuilder();
    String[] arr = name.split("\\.");

    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        s.append(".");
      }
      if (BeeUtils.equalsTrim(arr[i], "*")) {
        s.append(arr[i]);
      } else {
        s.append(builder.sqlQuote(arr[i]));
      }
    }
    return s.toString();
  }

  @Override
  public String getValue() {
    return name;
  }
}
