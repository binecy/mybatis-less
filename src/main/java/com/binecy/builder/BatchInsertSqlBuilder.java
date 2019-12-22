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

        ColumnDesc[] columnDesc = getColumnInfo(ctx);
        if(columnDesc == null) {
            return null;
        }
        BatchSqlContainer sqlContainer = new BatchSqlContainer(ctx);

        Class<?> actClass = ctx.getMappingClass();
        final String className = ReflectUtil.getClassName(actClass);

        for (ColumnDesc column : columnDesc) {
            sqlContainer.addInsertColumnSql(column.getColumnName());
            sqlContainer.addInsertValSql("#{" + className + "." + column.getPropertyName() + "}");
        }

        return sqlContainer.toBatchInsertSql(ctx.getTableName(), columnDesc[0].getParamName(), ReflectUtil.getClassName(ctx.getMappingClass()));
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }

    private ColumnDesc[] getColumnInfo(SqlBuilderContext ctx) {
        ColumnDesc[] columnMetadata = null;
        InsertProperty insertPropertyAnt = ctx.getMethod().getAnnotation(InsertProperty.class);
        if(insertPropertyAnt != null) {
            if(insertPropertyAnt.value().length > 0) {
                columnMetadata = ctx.getHelper().getColumnFromAnnotationVal(insertPropertyAnt.value(), 0, ctx);
            } else if(insertPropertyAnt.id().length() > 0) {
                String[] properties = ctx.getInsertPropertyById(insertPropertyAnt.id()).value();
                columnMetadata = ctx.getHelper().getColumnFromAnnotationVal(properties, 0, ctx);
            }
        } else {
            columnMetadata = ctx.getHelper().getColumnFromProperty(0, ctx);
        }
        return columnMetadata;
    }
}
