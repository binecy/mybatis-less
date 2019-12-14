package com.binecy.builder;

import com.binecy.annotation.InsertProperty;
import com.binecy.util.ColumnDesc;
import com.binecy.util.SqlBuilderHelper;
import org.apache.ibatis.mapping.SqlCommandType;

public class InsertSqlBuilder  implements SqlBuilder{
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        if(ctx.getParameterCount() != 1) {
            // 只支持一个entity或一个entity集合
            return null;
        }

        SqlBuilderHelper helper = ctx.getHelper();
        String tableName = ctx.getTableName();

        boolean isCollectionOpt = helper.isCollection(ctx.getMethod().getParameterTypes()[0]);

        ColumnDesc[] columnDesc = getColumnInfo(ctx);

        SqlContainer sqlContainer = new SqlContainer(ctx);

        if (isCollectionOpt) {
            return ctx.getBatchInsertSqlBuilder().buildSql(ctx);
        } else {
            for (ColumnDesc info : columnDesc) {
                sqlContainer.addInsertColumnSql(info.getColumnName());
                sqlContainer.addInsertValSql("#{" + info.getPropertyName() + "}");
            }
            return sqlContainer.toInsertSql(tableName);
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.INSERT;
    }

    public ColumnDesc[] getColumnInfo(SqlBuilderContext ctx) {
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
