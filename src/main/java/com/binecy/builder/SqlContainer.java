package com.binecy.builder;



import com.binecy.util.ColumnDesc;

import java.util.ArrayList;
import java.util.List;

public class SqlContainer {
    public SqlContainer(SqlBuilderContext ctx) {
        ctx.setSqlContainer(this);
    }

    private static final String INSERT_SQL_TEMP = "<script>insert into {{tableName}}({{tableColumn}})" +
            "values({{insertValue}})</script>";
    private static final String SELECT_SQL_TEMP = "<script>select {{tableColumn}} from {{tableName}}" +
            " {{whereColumnSql}} {{groupBy}} {{orderBy}}</script>" ;
    protected static final String UPDATE_SQL_TEMP =
            "<script>update {{tableName}} <set>{{updateValue}}</set> {{whereColumnSql}}</script>";
    private static final String DELETE_SQL_TEMP = "<script>delete from {{tableName}}" +
            " {{whereColumnSql}}</script>";

    public static final String TABLE_NAME_TAG = "{{tableName}}";
    public static final String WHERE_COLUMN_TAG = "{{whereColumnSql}}";
    public static final String TABLE_COLUMN_TAG = "{{tableColumn}}";
    public static final String INSERT_VAL_TAG = "{{insertValue}}";
    public static final String UPDATE_VAL_TAG = "{{updateValue}}";
    public static final String ORDER_BY_TAG = "{{orderBy}}";
    public static final String GROUP_BY_TAG = "{{groupBy}}";


    protected List<String> insertColumnParts = new ArrayList<>();
    protected List<String> insertValParts = new ArrayList<>();

    protected List<IfConditionContainer> updateColumnParts = new ArrayList<>();
    protected List<String> selectColumnParts = new ArrayList<>( );
    protected List<IfConditionContainer> whereColumnPart = new ArrayList<>();

    private String orderBySql;
    private String groupBySql;
    private String enhanceWhereSql;

    public void addInsertValSql(String sql) {
        insertValParts.add(sql);
    }

    public void addInsertColumnSql(String sql) {
        insertColumnParts.add(sql);
    }

    public void addSelectColumnParts(String part) {
        selectColumnParts.add(part);
    }

    public void addUpdateColumnPart(String setValSql, ColumnDesc columnDesc) {
        updateColumnParts.add(IfConditionContainer.of(setValSql, getIgnoreNullConditionIfNeed(columnDesc, null)));
    }

    public void addUpdateColumnByPropertyPart(String setValSql, ColumnDesc columnDesc) {
        updateColumnParts.add(IfConditionContainer.of(setValSql, getIgnoreNullConditionInProperty(columnDesc, null)));
    }

    public void addWhereColumnPart(String whereColumnSql, ColumnDesc columnDesc) {
        whereColumnPart.add(IfConditionContainer.of(whereColumnSql, getIgnoreNullConditionIfNeed(columnDesc, null)));
    }

    public String toInsertSql(String tableName) {
        String insertColumnSql = sqlJoin("," , insertColumnParts);
        String insertValSql = sqlJoin(",", insertValParts);

        return INSERT_SQL_TEMP.replace(TABLE_NAME_TAG, tableName)
                .replace(TABLE_COLUMN_TAG, insertColumnSql)
                .replace(INSERT_VAL_TAG, insertValSql);
    }


    public String toSelectSql(String tableName) {
        String selectColumnSql = "*";
        if(selectColumnParts != null) {
            selectColumnSql = sqlJoin(",",selectColumnParts);
        }

        return SELECT_SQL_TEMP.replace(TABLE_NAME_TAG, tableName)
                .replace(TABLE_COLUMN_TAG, selectColumnSql)
                .replace(WHERE_COLUMN_TAG, toWhereSql())
                .replace(ORDER_BY_TAG, orderBySql != null ? orderBySql : "")
                .replace(GROUP_BY_TAG, groupBySql != null ? groupBySql : "");
    }

    public String toUpdateSql(String tableName) {
        String updateColumnSql = ifConditionJoin(",", updateColumnParts, false);


        return UPDATE_SQL_TEMP.replace(TABLE_NAME_TAG, tableName)
                .replace(UPDATE_VAL_TAG, updateColumnSql)
                .replace(WHERE_COLUMN_TAG, toWhereSql());
    }

    protected String toWhereSql() {
        if(enhanceWhereSql != null) {
            return "<where>" + enhanceWhereSql + "</where>";
        }

        String whereSql = ifConditionJoin(" and ", whereColumnPart, true);

        if(whereSql == null || whereSql.length() == 0) {
            return "";
        }
        return "<where>" + whereSql + "</where>";
    }

    public String toDeleteSql(String tableName) {
        return DELETE_SQL_TEMP.replace(TABLE_NAME_TAG, tableName).
                replace(WHERE_COLUMN_TAG, toWhereSql());
    }

    public String getIgnoreNullConditionIfNeed(ColumnDesc columnDesc, String actSql) {
        if(!columnDesc.isIgnoreNull()) {
            return actSql;
        }

        String ignoreNullScr =
                "<if test='" + columnDesc.getParamName() + " != null{{checkCollectionSize}}'>";
        return generateIfCondition(ignoreNullScr, columnDesc, actSql);
    }

    private String getIgnoreNullConditionInProperty(ColumnDesc columnDesc, String actSql) {
        if(!columnDesc.isIgnoreNull()) {
            return actSql;
        }

        String ignoreNullScr =
                "<if test='" + columnDesc.getParamName() + "." + columnDesc.getPropertyName() + " != null{{checkCollectionSize}}'>";
        return generateIfCondition(ignoreNullScr, columnDesc, actSql);
    }

    private String generateIfCondition(String sqlStart, ColumnDesc columnDesc, String actSql) {
        if (columnDesc.isMultiValueParam()) {
            if(columnDesc.isArrayParam()) {
                sqlStart = sqlStart.replace("{{checkCollectionSize}}", " and " + columnDesc.getParamName() + ".length > 0");
            } else {
                sqlStart = sqlStart.replace("{{checkCollectionSize}}", " and " + columnDesc.getParamName() + ".size > 0");
            }
        } else {
            sqlStart = sqlStart.replace("{{checkCollectionSize}}", "");
        }
        return sqlStart  + (actSql != null ? actSql :  "{{actualSql}}") + "</if>";
    }

    public String getForeachSql(String columnName, String paramName, String filterCondition) {
        return columnName + filterCondition +
                "<foreach collection='" + paramName + "' item='item' open='(' close=') ' separator=','>" +
                "#{item}" +
                "</foreach>";
    }

    protected String sqlJoin(String split, List<String> sqls) {
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < sqls.size(); i++) {
            if(i > 0) {
                rs.append(split);
            }
            rs.append(sqls.get(i));
        }
        return rs.toString();
    }

    protected String ifConditionJoin(String split, List<IfConditionContainer> pairs, boolean splitInFront) {
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < pairs.size(); i++) {
            String sql = pairs.get(i).actualSql;

            if(splitInFront && i > 0) {
                sql = split + sql;
            } else if(!splitInFront && i < pairs.size() - 1) {
                sql = sql + split;
            }

            String ifCondition = pairs.get(i).ifCondition;
            if(ifCondition != null) {
                sql = ifCondition.replace("{{actualSql}}", sql);
            }

            rs.append(sql);
        }
        return rs.toString();
    }

    public String getOrderBySql() {
        return orderBySql;
    }

    public void setOrderBySql(String orderBySql) {
        this.orderBySql = orderBySql;
    }

    public String getGroupBySql() {
        return groupBySql;
    }

    public void setGroupBySql(String groupBySql) {
        this.groupBySql = groupBySql;
    }

    public String getEnhanceWhereSql() {
        return enhanceWhereSql;
    }

    public void setEnhanceWhereSql(String enhanceWhereSql) {
        this.enhanceWhereSql = enhanceWhereSql;
    }

    static class IfConditionContainer {
        String ifCondition;
        String actualSql;

        static IfConditionContainer of(String actSql, String ifCondition) {
            IfConditionContainer result = new IfConditionContainer();
            result.ifCondition = ifCondition;
            result.actualSql = actSql;
            return result;
        }
    }
}
