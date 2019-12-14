package com.binecy.builder;



public class BatchSqlContainer extends SqlContainer {
    public BatchSqlContainer(SqlBuilderContext ctx) {
        super(ctx);
    }

    private static final String BATCH_INSERT_SQL_TEMPLATE = "<script>insert into {{tableName}}({{tableColumn}}) values" +
            "<foreach collection ='{{paramName}}' item='{{className}}' index= 'index' separator =','>" +
            "({{insertValue}})" +
            "</foreach></script>";

    public String toBatchInsertSql(String tableName, String paramName, String className) {
        String insertColumnSql = sqlJoin("," , insertColumnParts);
        String insertValSql = sqlJoin(",", insertValParts);

        return BATCH_INSERT_SQL_TEMPLATE.replace(TABLE_NAME_TAG, tableName)
                .replace(TABLE_COLUMN_TAG, insertColumnSql)
                .replace(INSERT_VAL_TAG, insertValSql)
                .replace("{{paramName}}", paramName)
                .replace("{{className}}", className);
    }

    public String toBatchUpdateSql(String tableName) {
        String updateColumnSql = ifConditionJoin(",", updateColumnParts, false);
        return UPDATE_SQL_TEMP.replace(TABLE_NAME_TAG, tableName)
                .replace(UPDATE_VAL_TAG, updateColumnSql)
                .replace(WHERE_COLUMN_TAG, toWhereSql());
    }
}
