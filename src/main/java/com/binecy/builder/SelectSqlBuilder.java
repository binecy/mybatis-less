package com.binecy.builder;


import com.binecy.annotation.SelectProperty;
import com.binecy.util.ColumnDesc;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Method;

public class SelectSqlBuilder implements SqlBuilder {

    @Override
    public String buildSql(SqlBuilderContext ctx) {
        ColumnDesc[] columnDesc = getColumnDesc(ctx);
        if(columnDesc == null) {
            return null;
        }

        SqlContainer sqlContainer = new SqlContainer(ctx);
        if(columnDesc != null) {
            for (ColumnDesc info : columnDesc) {
                sqlContainer.addSelectColumnParts(info.getColumnName());
            }
        }

        ctx.getWhereSqlBuilder().buildSql(ctx);
        ctx.getGroupBySqlBuilder().buildSql(ctx);
        ctx.getOrderBySqlBuilder().buildSql(ctx);

        return sqlContainer.toSelectSql(ctx.getTableName());
    }

    private ColumnDesc[] getColumnDesc(SqlBuilderContext ctx) {
        Method method = ctx.getMethod();
        SelectProperty selectPropertyAnt = method.getAnnotation(SelectProperty.class);

        ColumnDesc[] columnDesc = null;
        if (selectPropertyAnt != null ) {
            if(selectPropertyAnt.value().length > 0) {
                columnDesc = ctx.getHelper().getColumnFromAnnotationVal(selectPropertyAnt.value(), -1, ctx);
            } else if(selectPropertyAnt.id().length() > 0){
                String[] properties = ctx.getSelectPropertyById(selectPropertyAnt.id()).value();
                columnDesc = ctx.getHelper().getColumnFromAnnotationVal(properties, -1, ctx);
            }
        } else {
            columnDesc = ctx.getHelper().getColumnFromProperty(-1, ctx);
        }
        return columnDesc;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }
}
