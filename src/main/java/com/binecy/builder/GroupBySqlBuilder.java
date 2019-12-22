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

        Group groupAnt = ctx.getMethod().getAnnotation(Group.class);
        String s = "group by " + groupAnt.by();
        if(!"".equals(groupAnt.having())) {
            s += " having " + groupAnt.having();
        }

        ctx.getSqlContainer().setGroupBySql(s);
        return null;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UNKNOWN;
    }
}
