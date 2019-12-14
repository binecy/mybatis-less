package com.binecy.builder;

import com.binecy.annotation.UpdateProperty;
import com.binecy.util.Parameter;
import com.binecy.util.ColumnDesc;
import com.binecy.util.SqlBuilderHelper;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UpdateSqlBuilder implements SqlBuilder {
    private static final Log log = LogFactory.getLog(UpdateSqlBuilder.class);

    @Override
    public String buildSql(SqlBuilderContext ctx) {
        Method method = ctx.getMethod();
        Parameter[] parameters = ctx.getParamInfo();
        SqlBuilderHelper helper = ctx.getHelper();

        int updatePropertyAnnotationIndex = getUpdatePropertyAnnotationIndex(parameters);

        // 参数中使用了UpdateProperty注解
        if (updatePropertyAnnotationIndex  >= 0) {
            return updateProperty(ctx);
        } else {
            if (parameters.length == 1 && helper.isCollection(method.getParameterTypes()[0])) {
                return ctx.getBatchUpdateSqlBuilder().buildSql(ctx);
            } else if (parameters[parameters.length - 1].getType().isAssignableFrom(ctx.getMappingClass())) {
                return updateEntity(ctx);
            } else {
                log.warn("Update Parameter Illegal:" + ctx.getBelongInterface().getSimpleName() + "." + method.getName());
                return null;
            }
        }
    }

    /**
     * 格式: update(int id, @UpdateProperty String name, String code)
     * @param ctx
     * @return
     */
    private String updateProperty(SqlBuilderContext ctx) {
        if (ctx.getParameterCount() == 0) {
            log.warn("MybatisLess builder err(method:" +  ctx.getMethod().getName() +"), parameterCount is error");
            return null;
        }

        Parameter[] parameters = ctx.getParamInfo();
        int updatePropertyAnnotationIndex = getUpdatePropertyAnnotationIndex(parameters);

        SqlBuilderHelper helper = ctx.getHelper();
        ColumnDesc[] columns = helper.getColumnFromParam(updatePropertyAnnotationIndex, ctx.getParameterCount() , ctx);

        SqlContainer sqlContainer = new SqlContainer(ctx);
        for (ColumnDesc column : columns) {
            sqlContainer.addUpdateColumnPart(column.getColumnName() + "=#{" + column.getParamName() + "}",
                    column);
        }

        ctx.setWhereParamStart(0);
        ctx.setWhereParamEnd(updatePropertyAnnotationIndex);
        ctx.getWhereSqlBuilder().buildSql(ctx);

        return sqlContainer.toUpdateSql(ctx.getTableName());
    }


    /**
     * 格式： update(int id, Subject subject)
     * @param ctx
     * @return
     */
    private String updateEntity(SqlBuilderContext ctx) {
        SqlBuilderHelper helper = ctx.getHelper();

        Method method = ctx.getMethod();
        Parameter[] parameters = ctx.getParamInfo();

        UpdateProperty updatePropertyAnnotation = method.getAnnotation(UpdateProperty.class);
        String[] updateProperties = null;
        if(updatePropertyAnnotation != null && updatePropertyAnnotation.value().length > 0) {
            updateProperties = helper.splitProperties(updatePropertyAnnotation.value());
        }
        ColumnDesc[] columns = helper.getColumnFromProperty(parameters.length - 1, ctx, updateProperties);

        final Set<String> ignoreNullProperty = getIgnoreNullProperty(method, ctx);

        SqlContainer sqlParts = new SqlContainer(ctx);
        for (ColumnDesc column : columns) {
            column.setIgnoreNull(ignoreNullProperty.contains(column.getPropertyName()));
            sqlParts.addUpdateColumnByPropertyPart(column.getColumnName() + "=#{" + column.getParamName() + "." + column.getPropertyName() + "}",
                    column);
        }

        ctx.setWhereParamStart(0);
        ctx.setWhereParamEnd(parameters.length - 1);    // where参数不包括最后的更新实体
        ctx.getWhereSqlBuilder().buildSql(ctx);

        return sqlParts.toUpdateSql(ctx.getTableName());
    }

    private Set<String> getIgnoreNullProperty(Method method, SqlBuilderContext ctx) {
        Set<String> ignoreNullProperty = new HashSet<String>();
        UpdateProperty updatePropertyAnnotation = method.getAnnotation(UpdateProperty.class);
        if (updatePropertyAnnotation != null && updatePropertyAnnotation.ignoreNullProperty() != null) {
            String[] ignoreNullPropertyStr = ctx.getHelper().splitProperties(updatePropertyAnnotation.ignoreNullProperty());
            ignoreNullProperty.addAll(Arrays.asList(ignoreNullPropertyStr));
        }
        return ignoreNullProperty;
    }

    private int getUpdatePropertyAnnotationIndex(Parameter[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(UpdateProperty.class) != null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UPDATE;
    }


}
