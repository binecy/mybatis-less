package com.binecy.builder;

import org.apache.ibatis.mapping.SqlCommandType;

public interface SqlBuilder {

    String buildSql(SqlBuilderContext ctx);

    SqlCommandType getSqlCommandType();
}
