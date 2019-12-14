package com.binecy.builder;

import com.binecy.annotation.InsertProperty;
import com.binecy.util.ColumnDesc;
import com.binecy.util.ReflectUtil;
import org.apache.ibatis.mapping.SqlCommandType;

public class BatchInsertSqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        if(ctx.getParameterCount() != 1) {
            // 只支持一个entity或一个entity集合
            return null;
        }

        String tableName = ctx.getTableName();
        ColumnDesc[] columnDesc = getColumnInfo(ctx);
        BatchSqlContainer sqlContainer = new BatchSqlContainer(ctx);

        Class<?> actClass = ctx.getMappingClass();
        final String className = ReflectUtil.getClassName(actClass);

        for (ColumnDesc column : columnDesc) {
            sqlContainer.addInsertColumnSql(column.getColumnName());
            sqlContainer.addInsertValSql("#{" + className + "." + column.getPropertyName() + "}");
        }

        return sqlContainer.toBatchInsertSql(tableName, columnDesc[0].getParamName(), ReflectUtil.getClassName(ctx.getMappingClass()));
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }

    private ColumnDesc[] getColumnInfo(SqlBuilderContext ctx) {
        ColumnDesc[] columnMetadata;
        InsertProperty insertPropertyAnnotation = ctx.getMethod().getAnnotation(InsertProperty.class);
        if(insertPropertyAnnotation != null) {
            columnMetadata = ctx.getHelper().getColumnFromAnnotationVal(insertPropertyAnnotation.value(), 0, ctx);
        } else {
            columnMetadata = ctx.getHelper().getColumnFromProperty(0, ctx);
        }
        return columnMetadata;
    }
}
