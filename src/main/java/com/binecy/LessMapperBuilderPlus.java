package com.binecy;

import com.binecy.annotation.ColumnMapping;
import com.binecy.annotation.TableMapping;
import com.binecy.builder.GroupBySqlBuilder;
import com.binecy.builder.OrderBySqlBuilder;
import com.binecy.builder.SqlBuilder;
import com.binecy.builder.SqlBuilderContext;
import com.binecy.util.ReflectUtil;
import com.binecy.util.SqlBuilderHelper;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import com.binecy.util.Parameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public class LessMapperBuilderPlus {
    private final Configuration configuration;
    private final Class<?> type;
    private Map<String, SqlBuilder> methodPreAndSqlBuilderMapper;
    private static final Set<Class<? extends Annotation>> MYBATIS_ANNOTATION_TYPES = new HashSet<>();
    private static final Log log = LogFactory.getLog(LessMapperBuilderPlus.class);

    private static final String UNDERLINE_CONFIG = "mybatisLess.mapping.toUnderLine";
    static {
        // mybatis 注解
        MYBATIS_ANNOTATION_TYPES.add(Select.class);
        MYBATIS_ANNOTATION_TYPES.add(Insert.class);
        MYBATIS_ANNOTATION_TYPES.add(Update.class);
        MYBATIS_ANNOTATION_TYPES.add(Delete.class);

        MYBATIS_ANNOTATION_TYPES.add(SelectProvider.class);
        MYBATIS_ANNOTATION_TYPES.add(InsertProvider.class);
        MYBATIS_ANNOTATION_TYPES.add(UpdateProvider.class);
        MYBATIS_ANNOTATION_TYPES.add(DeleteProvider.class);
    }

    public LessMapperBuilderPlus(Configuration configuration, Class<?> type, Map<String, SqlBuilder> methodPreAndSqlBuilderMapper) {
        this.configuration = configuration;
        this.type = type;
        this.methodPreAndSqlBuilderMapper = methodPreAndSqlBuilderMapper;
    }

    private boolean hasMybatisAnnotation(Method method) {
        for (Class<? extends Annotation> mybatisAnnotationType : MYBATIS_ANNOTATION_TYPES) {
            if (method.getAnnotation(mybatisAnnotationType) != null)
                return true;
        }
        return false;
    }

    // 插入到MapperAnnotationBuilder的getSqlSourceFromAnnotations方法前
    public SqlSource createSqlSourceFromAnnotations(Method method, Class<?> parameterType, LanguageDriver languageDriver) {
        if (type.getAnnotation(TableMapping.class) == null) {
            return null;
        }

        // 有mybatis注解，不处理
        if (hasMybatisAnnotation(method)) {
            return null;
        }

        String methodName = method.getName();
        SqlBuilder sqlBuilder = null;
        for (Map.Entry<String, SqlBuilder> entry : methodPreAndSqlBuilderMapper.entrySet()) {
            if (methodName.startsWith(entry.getKey())) {
                sqlBuilder = entry.getValue();
                break;
            }
        }

        if(sqlBuilder == null) {
            return null;
        }

        TableMapping tableMappingAnnotation = type.getAnnotation(TableMapping.class);
        SqlBuilderContext ctx = new SqlBuilderContext();

        ctx.setWhereSqlBuilder(methodPreAndSqlBuilderMapper.get("where"));
        ctx.setBatchInsertSqlBuilder(methodPreAndSqlBuilderMapper.get("batchInsert"));
        ctx.setBatchUpdateSqlBuilder(methodPreAndSqlBuilderMapper.get("batchUpdate"));
        ctx.setSelectSqlBuilder(methodPreAndSqlBuilderMapper.get("select"));
        ctx.setGroupBySqlBuilder(new GroupBySqlBuilder());
        ctx.setOrderBySqlBuilder(new OrderBySqlBuilder());

        ctx.setBelongInterface(type);
        ctx.setMethod(method);
        ctx.setMappingClass(tableMappingAnnotation.mappingClass());

        SqlBuilderHelper helper = new SqlBuilderHelper();
        ctx.setHelper(helper);
        // 是否驼峰转下划线
        if (configuration.getVariables() != null
                && configuration.getVariables().getProperty(UNDERLINE_CONFIG) != null) {
            helper.setToUnderLine(Boolean.valueOf(configuration.getVariables().getProperty(UNDERLINE_CONFIG)));
        } else {
            helper.setToUnderLine(true);
        }
        // 表名
        if (tableMappingAnnotation.tableName().length() > 0) {
            ctx.setTableName(tableMappingAnnotation.tableName());
        } else {
            ctx.setTableName(helper.toUnderLineIfNeed(tableMappingAnnotation.mappingClass().getSimpleName()));
        }

        // 设置属性和列名映射
        setColumnMapping(tableMappingAnnotation, ctx);
        // 设置参数信息
        setParamInfo(method, ctx);
        // 设置忽略的类属性
        setIgnoreProperties(tableMappingAnnotation, ctx);

        try {
            String sqlStr = sqlBuilder.buildSql(ctx);
            log.debug("SuccessBuildSql:" + type.getSimpleName() + "." + methodName + " --> " + sqlStr);
            if (sqlStr == null) {
                return null;
            }

            return buildSqlSourceFromStrings(new String[]{sqlStr}, parameterType, languageDriver);
        } catch (Exception e) {
            log.error("Error Building Sql:" + type.getSimpleName() + "." + methodName, e);
            return null;
        }
    }

    private void setColumnMapping(TableMapping tableMappingAnnotation, SqlBuilderContext ctx) {
        ColumnMapping[] columnMappings = tableMappingAnnotation.columnMapping();
        if (columnMappings.length > 0) {
            Map<String, String> propertyColumnMapping = new HashMap<>();
            for (ColumnMapping result : columnMappings) {
                propertyColumnMapping.put(result.property(), result.column());
            }
            ctx.setPropertyColumnMapping(propertyColumnMapping);
        }
    }

    private void setIgnoreProperties(TableMapping classMapperAnnotation,SqlBuilderContext ctx) {
        String[]  ignoreProperties = ctx.getHelper().splitProperties(classMapperAnnotation.ignoreProperty());
        ctx.setIgnoreProperties(ignoreProperties);
    }

    private void setParamInfo(Method method, SqlBuilderContext context) {
        // 通过Method类是否存在getParameters方法，是否为jdk8的版本
        boolean fromJdk8 = false;
        for (Method method2 : method.getClass().getMethods()) {
            if(method2.getName().equals("getParameters")) {
                // jdk8
                fromJdk8 = true;
                break;
            }
        }

        Parameter[] paramInfo = null;
        Class<?>[] paramTypes = method.getParameterTypes();
        if(fromJdk8) {
            // jdk7下只能反射调用
            Object[] parameters = (Object[]) ReflectUtil.invokeMethod(method, "getParameters");
            paramInfo = new Parameter[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Annotation[] annotations = (Annotation[]) ReflectUtil.invokeMethod(parameters[i], "getAnnotations");
                Class<?> type = (Class<?>) ReflectUtil.invokeMethod(parameters[i], "getType");
                String name = (String) ReflectUtil.invokeMethod(parameters[i], "getName");
                paramInfo[i] = new Parameter(annotations, type, name);
            }
        } else {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            paramInfo = new Parameter[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                Parameter p = new Parameter(parameterAnnotations[i], paramTypes[i], i);
                paramInfo[i] = p;
            }
        }

        context.setParamInfo(paramInfo);
        context.setParameterCount(paramTypes.length);
    }

    // 来自MapperAnnotationBuilder的私有方法
    private SqlSource buildSqlSourceFromStrings(String[] strings, Class<?> parameterTypeClass, LanguageDriver languageDriver) {
        final StringBuilder sql = new StringBuilder();
        for (String fragment : strings) {
            sql.append(fragment);
            sql.append(" ");
        }
        return languageDriver.createSqlSource(configuration, sql.toString().trim(), parameterTypeClass);
    }

    // 在MapperAnnotationBuilder的getSqlCommandType方法前调用
    public SqlCommandType getSqlCommandType(Method method) {
        if (type.getAnnotation(TableMapping.class) == null) {
            return null;
        }
        if (hasMybatisAnnotation(method)) {
            return null;
        }
        String methodName = method.getName();
        for (Map.Entry<String, SqlBuilder> entry : methodPreAndSqlBuilderMapper.entrySet()) {
            if (methodName.startsWith(entry.getKey())) {
                return entry.getValue().getSqlCommandType();
            }
        }
        return null;
    }
}
