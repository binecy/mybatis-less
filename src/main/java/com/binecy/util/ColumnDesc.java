package com.binecy.util;

public class ColumnDesc {
    private String columnName;        // 列名
    private String propertyName;   // 字段名

    // 优先级 @Param > (mybatis默认的单参数命名)单集合参数collection 单数组参数array 单参数_parameter > parameter.getName
    private String paramName;
    // 集合或者数组参数
    private boolean isMultiValueParam;
    private boolean isArrayParam;

    private String likeVal;
    // 关系运算符
    private String relationalOperator;
    private boolean ignoreNull;

    public boolean isIgnoreNull() {
        return ignoreNull;
    }

    public void setIgnoreNull(boolean ignoreNull) {
        this.ignoreNull = ignoreNull;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public boolean isMultiValueParam() {
        return isMultiValueParam;
    }

    public void setMultiValueParam(boolean multiValueParam) {
        isMultiValueParam = multiValueParam;
    }

    public String getLikeVal() {
        return likeVal;
    }

    public void setLikeVal(String likeVal) {
        this.likeVal = likeVal;
    }

    public String getRelationalOperator() {
        return relationalOperator;
    }

    public void setRelationalOperator(String relationalOperator) {
        this.relationalOperator = relationalOperator;
    }

    public boolean isArrayParam() {
        return isArrayParam;
    }

    public void setArrayParam(boolean arrayParam) {
        isArrayParam = arrayParam;
    }
}
