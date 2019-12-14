package com.binecy.builder;

import com.binecy.util.Parameter;
import com.binecy.util.SqlBuilderHelper;

import java.lang.reflect.Method;
import java.util.Map;

public class SqlBuilderContext {
    private Method method;  // 方法
    private Class<?> belongInterface;   // 方法所在接口
    private Class<?> mappingClass;      // 表映射对象
    private String tableName;           // 表名
    private Integer whereParamStart;    // 方法中用于where的参数开始下标
    private Integer whereParamEnd;      // 方法中用于where的参数结束下标
    private Map<String, String> propertyColumnMapping;  // 属性名和表列名对应
    private Parameter[] paramInfo;      // 方法参数信息
    private int parameterCount;         // 方法参数个数
    private String[] ignoreProperties;  // 映射类中忽略的属性
    private SqlBuilderHelper helper;    // 辅助类
    private SqlContainer sqlContainer;  // sql片段容器

    private SqlBuilder batchInsertSqlBuilder;
    private SqlBuilder batchUpdateSqlBuilder;
    private SqlBuilder whereSqlBuilder;
    private SqlBuilder orderBySqlBuilder;
    private SqlBuilder groupBySqlBuilder;
    private SqlBuilder selectSqlBuilder;


    public SqlBuilder getOrderBySqlBuilder() {
        return orderBySqlBuilder;
    }

    public void setOrderBySqlBuilder(SqlBuilder orderBySqlBuilder) {
        this.orderBySqlBuilder = orderBySqlBuilder;
    }

    public SqlBuilder getGroupBySqlBuilder() {
        return groupBySqlBuilder;
    }

    public void setGroupBySqlBuilder(SqlBuilder groupBySqlBuilder) {
        this.groupBySqlBuilder = groupBySqlBuilder;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getBelongInterface() {
        return belongInterface;
    }

    public void setBelongInterface(Class<?> belongInterface) {
        this.belongInterface = belongInterface;
    }

    public Class<?> getMappingClass() {
        return mappingClass;
    }

    public void setMappingClass(Class<?> mappingClass) {
        this.mappingClass = mappingClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getWhereParamStart() {
        return whereParamStart;
    }

    public void setWhereParamStart(Integer whereParamStart) {
        this.whereParamStart = whereParamStart;
    }

    public Integer getWhereParamEnd() {
        return whereParamEnd;
    }

    public void setWhereParamEnd(Integer whereParamEnd) {
        this.whereParamEnd = whereParamEnd;
    }

    public Map<String, String> getPropertyColumnMapping() {
        return propertyColumnMapping;
    }

    public void setPropertyColumnMapping(Map<String, String> propertyColumnMapping) {
        this.propertyColumnMapping = propertyColumnMapping;
    }

    public Parameter[] getParamInfo() {
        return paramInfo;
    }

    public void setParamInfo(Parameter[] paramInfo) {
        this.paramInfo = paramInfo;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public void setParameterCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    public String[] getIgnoreProperties() {
        return ignoreProperties;
    }

    public void setIgnoreProperties(String[] ignoreProperties) {
        this.ignoreProperties = ignoreProperties;
    }

    public SqlBuilderHelper getHelper() {
        return helper;
    }

    public void setHelper(SqlBuilderHelper helper) {
        this.helper = helper;
    }

    public SqlContainer getSqlContainer() {
        return sqlContainer;
    }

    public void setSqlContainer(SqlContainer sqlContainer) {
        this.sqlContainer = sqlContainer;
    }

    public SqlBuilder getBatchInsertSqlBuilder() {
        return batchInsertSqlBuilder;
    }

    public void setBatchInsertSqlBuilder(SqlBuilder batchInsertSqlBuilder) {
        this.batchInsertSqlBuilder = batchInsertSqlBuilder;
    }

    public SqlBuilder getBatchUpdateSqlBuilder() {
        return batchUpdateSqlBuilder;
    }

    public void setBatchUpdateSqlBuilder(SqlBuilder batchUpdateSqlBuilder) {
        this.batchUpdateSqlBuilder = batchUpdateSqlBuilder;
    }

    public SqlBuilder getWhereSqlBuilder() {
        return whereSqlBuilder;
    }

    public void setWhereSqlBuilder(SqlBuilder whereSqlBuilder) {
        this.whereSqlBuilder = whereSqlBuilder;
    }

    public SqlBuilder getSelectSqlBuilder() {
        return selectSqlBuilder;
    }

    public void setSelectSqlBuilder(SqlBuilder selectSqlBuilder) {
        this.selectSqlBuilder = selectSqlBuilder;
    }
}
