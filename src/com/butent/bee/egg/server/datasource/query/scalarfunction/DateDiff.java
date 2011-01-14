package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.data.value.DateTimeValue;
import com.butent.bee.egg.shared.data.value.DateValue;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;
import com.butent.bee.egg.shared.utils.TimeUtils;

import java.util.List;

public class DateDiff implements ScalarFunction {
  private static final String FUNCTION_NAME = "dateDiff";
  private static final DateDiff INSTANCE = new DateDiff();

  public static DateDiff getInstance() {
    return INSTANCE;
  }

  private DateDiff() {
  }

  public Value evaluate(List<Value> values) {
    Value firstValue = values.get(0);
    Value secondValue = values.get(1);

    if (firstValue.isNull() || secondValue.isNull()) {
      return NumberValue.getNullValue();
    }
    BeeDate firstDate = getDateFromValue(firstValue);
    BeeDate secondDate = getDateFromValue(secondValue);

    return new NumberValue(TimeUtils.dateDiff(secondDate, firstDate));
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ", " + argumentsQueryStrings.get(1)
        + ")";
  }

  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("Number of parameters for the dateDiff "
          + "function is wrong: " + types.size());
    } else if ((!isDateOrDateTimeValue(types.get(0)))
        || (!isDateOrDateTimeValue(types.get(1)))) {
      throw new InvalidQueryException("Can't perform the function 'dateDiff' "
          + "on values that are not a Date or a DateTime values");
    }
  }

  private BeeDate getDateFromValue(Value value) {
    if (value.getType() == ValueType.DATE) {
      return ((DateValue) value).getObjectToFormat();
    } else {
      return ((DateTimeValue) value).getObjectToFormat();
    }
  }
  
  private boolean isDateOrDateTimeValue(ValueType type) {
    return ((type == ValueType.DATE) || (type == ValueType.DATETIME));
  }
}
