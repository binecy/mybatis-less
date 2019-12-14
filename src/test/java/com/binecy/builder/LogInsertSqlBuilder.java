package com.binecy.builder;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

public class LogInsertSqlBuilder extends InsertSqlBuilder{
    private static final Log log = LogFactory.getLog(UpdateSqlBuilder.class);

    @Override
    public String buildSql(SqlBuilderContext ctx) {
        log.warn("build insert sql start:" + ctx.getBelongInterface().getSimpleName() + "." + ctx.getMethod().getName());
        return super.buildSql(ctx);
    }
}
