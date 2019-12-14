package com.binecy.builder;

import com.binecy.annotation.Order;
import org.apache.ibatis.mapping.SqlCommandType;

public class OrderBySqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        if(ctx.getSqlContainer().getOrderBySql() != null) {
            return null;
        }

        String s = null;
        if(ctx.getMethod().getAnnotation(Order.class) != null) {
            Order orderAnno = ctx.getMethod().getAnnotation(Order.class);
            s = "order by " + orderAnno.by();
            if(orderAnno.desc()) {
                s += " desc";
            }
        }

        ctx.getSqlContainer().setOrderBySql(s);
        return null;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UNKNOWN;
    }
}
