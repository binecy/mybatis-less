package com.binecy.builder;

import com.binecy.annotation.BatchUpdateKey;
import com.binecy.annotation.UpdateProperty;
import com.binecy.util.ColumnDesc;
import com.binecy.util.ReflectUtil;
import com.binecy.util.SqlBuilderHelper;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.Method;

public class BatchUpdateSqlBuilder implements SqlBuilder{
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        SqlBuilderHelper helper = ctx.getHelper();

        String tableName = ctx.getTableName();
        Method method = ctx.getMethod();

        Class<?> actType = ctx.getMappingClass();
        String className = ReflectUtil.getClassName(actType);

        UpdateProperty updatePropertyAnnotation = method.getAnnotation(UpdateProperty.class);
        String[] selectProperties = null;
        if(updatePropertyAnnotation != null && updatePropertyAnnotation.value().length > 0) {
            selectProperties = helper.splitProperties(updatePropertyAnnotation.value());
        }


        ColumnDesc[] tableColumns = helper.getColumnFromProperty(0, ctx, selectProperties);

        String batchUpdateKey = getBatchUpdateKey(method);

        BatchSqlContainer sqlContainer = new BatchSqlContainer(ctx);

        for (ColumnDesc column : tableColumns) {
            String s = "{{columnName}}=" +
                    "<foreach collection='{{paramName}}' item='{{className}}' index='index' " +
                    "separator=' ' open='case {{updateKey}} ' close=' end'>" +
                    "when #{{{className}}.{{updateKey}}} then #{{{className}}.{{propertyName}}}" +
                    "</foreach>";

            s = s.replace("{{columnName}}", column.getColumnName())
                    .replace("{{propertyName}}", column.getPropertyName())
                    .replace("{{paramName}}", column.getParamName())
                    .replace("{{className}}", className)
                    .replace("{{updateKey}}", batchUpdateKey);

            sqlContainer.addUpdateColumnPart(s, column);
        }

        String whereColumnSql = "{{updateKey}} in" +
                "    <foreach collection='{{paramName}}' index='index' item='{{className}}'" +
                "        separator=',' open='(' close=')'>" +
                "        #{{{className}}.{{updateKey}}}" +
                "    </foreach>";

        whereColumnSql = whereColumnSql.replace("{{updateKey}}", batchUpdateKey)
                .replace("{{paramName}}", tableColumns[0].getParamName())
                .replace("{{className}}", className);

        sqlContainer.setEnhanceWhereSql(whereColumnSql);

        return sqlContainer.toBatchUpdateSql(tableName);
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UPDATE;
    }

    private static final String DEFAULT_UPDATE_KEY = "id";
    private String getBatchUpdateKey(Method method) {
        BatchUpdateKey batchUpdateKeyAnnotation = method.getAnnotation(BatchUpdateKey.class);
        if (batchUpdateKeyAnnotation == null) {
            return DEFAULT_UPDATE_KEY;
        } else {
            return batchUpdateKeyAnnotation.value();
        }
    }

}
