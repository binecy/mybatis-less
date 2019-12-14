package com.binecy.builder;

import com.binecy.annotation.Group;
import org.apache.ibatis.mapping.SqlCommandType;

public class GroupBySqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        if(ctx.getSqlContainer().getGroupBySql() != null) {
            // 已经处理了
            return null;
        }

        if(ctx.getMethod().getAnnotation(Group.class) == null) {
            return null;
        }

        Group groupAnnotation = ctx.getMethod().getAnnotation(Group.class);
        String s = "group by " + groupAnnotation.by();
        if(!"".equals(groupAnnotation.having())) {
            s += " having " + groupAnnotation.having();
        }

        ctx.getSqlContainer().setGroupBySql(s);
        return null;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UNKNOWN;
    }
}
