package com.binecy.builder;


import com.binecy.annotation.SelectProperty;
import com.binecy.util.ColumnDesc;
import com.binecy.util.SqlBuilderHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Method;

public class SelectSqlBuilder implements SqlBuilder {

    @Override
    public String buildSql(SqlBuilderContext ctx) {
        String tableName = ctx.getTableName();
        ColumnDesc[] columnDesc = getColumnDesc(ctx);

        SqlContainer sqlContainer = new SqlContainer(ctx);
        if(columnDesc != null) {
            for (ColumnDesc info : columnDesc) {
                sqlContainer.addSelectColumnParts(info.getColumnName());
            }
        }

        ctx.getWhereSqlBuilder().buildSql(ctx);
        ctx.getGroupBySqlBuilder().buildSql(ctx);
        ctx.getOrderBySqlBuilder().buildSql(ctx);

        return sqlContainer.toSelectSql(tableName);
    }

    private ColumnDesc[] getColumnDesc(SqlBuilderContext ctx) {
        Method method = ctx.getMethod();
        SelectProperty selectPropertyAnnotation = method.getAnnotation(SelectProperty.class);

        ColumnDesc[] columnDesc = null;
        if (selectPropertyAnnotation != null ) {
            columnDesc = ctx.getHelper().getColumnFromAnnotationVal(selectPropertyAnnotation.value(), -1, ctx);
        } else if(ctx.getIgnoreProperties() != null || ctx.getIgnoreProperties().length >= 0){
            columnDesc = ctx.getHelper().getColumnFromProperty(-1, ctx);
        }
        return columnDesc;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }
}
