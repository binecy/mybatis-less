package com.binecy.builder;

import org.apache.ibatis.mapping.SqlCommandType;


public class DeleteSqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        String tableName = ctx.getTableName();

        SqlContainer sqlContainer = new SqlContainer(ctx);

        ctx.getWhereSqlBuilder().buildSql(ctx);
        return sqlContainer.toDeleteSql(tableName);
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.DELETE;
    }
}
