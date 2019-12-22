package com.binecy.builder;

import com.binecy.annotation.InsertProperty;
import com.binecy.util.ColumnDesc;
import org.apache.ibatis.mapping.SqlCommandType;

public class InsertSqlBuilder  implements SqlBuilder{
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

        SqlContainer sqlContainer = new SqlContainer(ctx);
        boolean isCollectionOpt = ctx.getHelper().isCollection(ctx.getMethod().getParameterTypes()[0]);
        if (isCollectionOpt) {
            return ctx.getBatchInsertSqlBuilder().buildSql(ctx);
        } else {
            for (ColumnDesc info : columnDesc) {
                sqlContainer.addInsertColumnSql(info.getColumnName());
                sqlContainer.addInsertValSql("#{" + info.getPropertyName() + "}");
            }
            return sqlContainer.toInsertSql(ctx.getTableName());
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }

    public ColumnDesc[] getColumnInfo(SqlBuilderContext ctx) {
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
