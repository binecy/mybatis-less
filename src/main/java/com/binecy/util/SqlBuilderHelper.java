package com.binecy.util;

import com.binecy.annotation.*;

import com.binecy.builder.SqlBuilderContext;
import org.apache.ibatis.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlBuilderHelper {
    private boolean isToUnderLine;   // 驼峰转下划线

    public ColumnDesc[] getColumnFromParam(List<Parameter> parameters, SqlBuilderContext ctx) {
        if(parameters == null || parameters.isEmpty()) {
            return new ColumnDesc[0];
        }

        List<ColumnDesc> result = new ArrayList<>();
        Field[] fields = ctx.getMappingClass().getDeclaredFields();

        for (Parameter parameter : parameters) {
            result.add(convertToColumn(parameter, fields, ctx));
        }
        return result.toArray(new ColumnDesc[0]);
    }

    // 从参数中获取列信息
    public ColumnDesc[] getColumnFromParam(int startIndex, int endIndex, SqlBuilderContext ctx) {
        Parameter[] parameters = ctx.getParamInfo();

        List<ColumnDesc> result = new ArrayList<>();

        Field[] fields = ctx.getMappingClass().getDeclaredFields();

        if(startIndex < 0 || endIndex < 0) {
            return new ColumnDesc[0];
        }

        for(int i = startIndex; i < endIndex; i++) {
            Parameter param = parameters[i];
            ColumnDesc column = convertToColumn(param, fields, ctx);
            result.add(column);
        }

        return result.toArray(new ColumnDesc[0]);
    }

    private ColumnDesc convertToColumn(Parameter param, Field[] fields, SqlBuilderContext ctx) {
        ColumnDesc column = new ColumnDesc();
        column.setParamName(getParamName(param, ctx.getMethod()));

        String annotationName = getAnnotationParamName(param);
        String propertyName = findOptimumProperty(fields, annotationName != null ? annotationName : param.getName());
        column.setPropertyName(propertyName);
        column.setColumnName(toColumnName(propertyName, ctx));

        column.setMultiValueParam(isCollection(param.getType()));

        Like likeAnt = param.getAnnotation(Like.class);
        if(likeAnt != null && likeAnt.value().length() > 0) {
            column.setLikeVal(likeAnt.value());
        }
        column.setRelationalOperator(getRelationalOperator(param));
        column.setIgnoreNull(param.getAnnotation(IgnoreNull.class)!=null);

        if(param.getType().isArray()) {
            column.setArrayParam(true);
        }
        return column;
    }

    private String getRelationalOperator(Parameter p) {
        if(isCollection(p.getType())) {
            return p.getAnnotation(NotIn.class) != null ? " not in " : " in ";
        }

        if (p.getAnnotation(NotEq.class) != null) {
            return "&lt;&gt;";
        } else if (p.getAnnotation(Lt.class) != null) {
            return "&lt;";
        } else if (p.getAnnotation(Gt.class) != null) {
            return "&gt;";
        } else if (p.getAnnotation(Like.class) != null) {
            return " like ";
        } else if (p.getAnnotation(GtEq.class) != null) {
            return "&gt;=";
        } else if (p.getAnnotation(LtEq.class) != null) {
            return "&lt;=";
        } else {
            return "=";
        }
    }


    // paramName可以比propertyName长,如propertyName为createTime, paramName可以为createTimeStart,createTimeEnd
    // 类中存在属性aa,aaa, 参数名aaab,查询属性应该是aaa
    public String findOptimumProperty(Field[] properties, String candidate) {
        Field matchProperty = null;
        int maxMatch = -1;
        for (Field classField : properties) {
            if (candidate.startsWith(classField.getName())) {
                if (classField.getName().length() > maxMatch) {
                    maxMatch = classField.getName().length();
                    matchProperty = classField;
                }
            }
        }

        if(matchProperty == null) {    // 非column，property的参数，如pageSize,pageNum
            return candidate;
        } else {
            return matchProperty.getName();
        }
    }

    // 从annotation配置获取列信息
    public ColumnDesc[] getColumnFromAnnotationVal(String[] annotations, int paramIndex, SqlBuilderContext ctx) {
        if(annotations == null)
            return null;

        annotations = splitProperties(annotations);

        String paramName = null;
        ColumnDesc[] result = new ColumnDesc[annotations.length];

        if(paramIndex >= 0) {
            Parameter parameter = ctx.getParamInfo()[paramIndex];
            paramName = getParamName(parameter, ctx.getMethod());
        }

        for (int i = 0; i < annotations.length; i++) {
            String annVal = annotations[i];

            ColumnDesc info = new ColumnDesc();
            info.setParamName(paramName);
            info.setPropertyName(annVal.trim());
            info.setColumnName(toColumnName(annVal.trim(), ctx));

            result[i] = info;
        }
        return result;
    }

    // 从类属性中获取列信息
    public ColumnDesc[] getColumnFromProperty(int paramIndex, SqlBuilderContext ctx) {
        return getColumnFromProperty(paramIndex, ctx, null);
    }

    public ColumnDesc[] getColumnFromProperty(int paramIndex, SqlBuilderContext ctx, String[] selectProperties) {
        String paramName = null;
        if(paramIndex >= 0) {
            Parameter parameter = ctx.getParamInfo()[paramIndex];
            paramName = getParamName(parameter, ctx.getMethod());
        }

        Field[] fields= ctx.getMappingClass().getDeclaredFields();
        fields = filterProperty(fields, ctx.getIgnoreProperties(), selectProperties);

        ColumnDesc[] result = new ColumnDesc[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            ColumnDesc info = new ColumnDesc();
            info.setParamName(paramName);

            info.setPropertyName(field.getName());
            info.setColumnName(toColumnName(field.getName(), ctx));
            result[i] = info;
        }

        return  result;
    }

    // 过滤属性
    private Field[] filterProperty(Field[] properties, String[] ignoreProperties, String[] selectProperties) {
        if((ignoreProperties == null || ignoreProperties.length == 0)
            && selectProperties == null) {
            return properties;
        }

        List<String> ignoreList = Arrays.asList(ignoreProperties);
        List<String> selectList = (selectProperties == null ? null : Arrays.asList(selectProperties));
        Set<Field> rs = new HashSet<>();
        for (Field field : properties) {
            if(!ignoreList.contains(field.getName())) {
                if(selectList == null) {
                    rs.add(field);
                } else if(selectList.contains(field.getName())) {
                    rs.add(field);
                }
            }
        }
        return rs.toArray(new Field[0]);
    }

    // 转化为列名
    private String toColumnName(String propertyName, SqlBuilderContext ctx) {
        if(ctx.getPropertyColumnMapping() == null || ctx.getPropertyColumnMapping().isEmpty()) {
            return toUnderLineIfNeed(propertyName);
        }

        String mappingName = ctx.getPropertyColumnMapping().get(propertyName);
        return mappingName != null ? mappingName : toUnderLineIfNeed(propertyName);
    }

    // 转下划线
    private static final Pattern TO_UNDER_LINE_PATTERN = Pattern.compile("[A-Z]");
    public String toUnderLineIfNeed(String str){
        if(!isToUnderLine || str == null)
            return str;

        Matcher matcher = TO_UNDER_LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()){
            String split = (matcher.start()) == 0 ? "" : "_";
            matcher.appendReplacement(sb, split +matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz)
                || clazz.isArray();
    }

    public String[] splitProperties(String[] annotationVal) {
        if(annotationVal == null || annotationVal.length == 0) {
            return new String[0];
        }

        if(annotationVal.length == 1) {
            annotationVal = annotationVal[0].trim().split(",");
        }
        return annotationVal;
    }

    private static final String DEFAULT_COLLECTION_NAME = "collection";
    private static final String DEFAULT_ARRAY_NAME = "array";
    private static final String DEFAULT_PARAM_NAME = "_parameter";
    public String getParamName(Parameter p, Method method) {
        String n = getAnnotationParamName(p);

        if(n != null)
            return n;

        // 如果是单参数，mybatis会给这个参数一个固定名
        if (method != null && method.getParameterTypes().length == 1 ) {
            if (isCollection(p.getType())) {
                return p.getType().isArray() ? DEFAULT_ARRAY_NAME : DEFAULT_COLLECTION_NAME;
            } else {
                return DEFAULT_PARAM_NAME;
            }
        }
        return p.getName();
    }

    private String getAnnotationParamName(Parameter parameter) {
        Param param = parameter.getAnnotation(Param.class);
        if(param != null) {
            return param.value();
        }
        return null;
    }

    public void setToUnderLine(boolean toUnderLine) {
        isToUnderLine = toUnderLine;
    }


}
