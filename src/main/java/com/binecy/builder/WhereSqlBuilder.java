package com.binecy.builder;

import com.binecy.annotation.*;
import com.binecy.util.Parameter;
import com.binecy.util.ColumnDesc;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhereSqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        Method method = ctx.getMethod();

        Condition conditionAnt = method.getAnnotation(Condition.class);
        if (conditionAnt == null) {
            ColumnDesc[] columnDesc = filterParam(ctx);
            generateWhereSql(columnDesc, ctx);
        } else {
            // 在condition中写了order by或group by要移到context
            String whereSql = extractGroupOrderSql(conditionAnt.value().trim(), ctx);

            ColumnDesc[] columnDesc = filterParam(ctx);
            String enhanceWhereSql = enhanceWhereSql(columnDesc,whereSql, ctx);
            ctx.getSqlContainer().setEnhanceWhereSql(enhanceWhereSql);
        }
        return null;
    }

    private ColumnDesc[] filterParam(SqlBuilderContext ctx) {
        // 有些情况下只有一部分参数是用于构建where sql， 如updateFields
        List<Parameter> filtered = new ArrayList<>();
        Parameter[] params = ctx.getParamInfo();
        int startIndex = 0, endIndex = params.length;
        if (ctx.getWhereParamStart() != null && ctx.getWhereParamEnd() != null) {
            if (ctx.getWhereParamEnd() < ctx.getWhereParamStart()) {
                throw new IllegalArgumentException("build sql error,method:" + ctx.getMethod().getName() + ",start:" + ctx.getWhereParamStart() + ",end" + ctx.getWhereParamEnd());
            } else {
                startIndex = ctx.getWhereParamStart();
                endIndex = ctx.getWhereParamEnd();
            }
        }

        // 过滤having语句中的参数
        String groupSql = ctx.getSqlContainer().getGroupBySql();
        if(groupSql == null) {
            Group groupAnt = ctx.getMethod().getAnnotation(Group.class);
            if(groupAnt != null) {
                groupSql = groupAnt.having();
            }
        }

        for(int i = startIndex; i < endIndex; i++) {
            Parameter parameter = params[i];
            if(groupSql == null) {
                filtered.add(parameter);
            } else if(!groupSql.contains(ctx.getHelper().getParamName(parameter, ctx.getMethod()))){
                filtered.add(parameter);
            }
        }

        return ctx.getHelper().getColumnFromParam(filtered, ctx);
    }

    private String generateWhereSql(ColumnDesc[] columns, SqlBuilderContext ctx) {
        if (columns == null || columns.length == 0) {
            return null;
        }

        SqlContainer sqlContainer = ctx.getSqlContainer();
        for (ColumnDesc column : columns) {
            String whereColumnSql;
            if(column.isMultiValueParam()) {
                whereColumnSql = sqlContainer.getForeachSql(column.getColumnName(), column.getParamName(), column.getRelationalOperator());
            } else if(column.getLikeVal() != null){
                whereColumnSql = column.getColumnName() + " like '" + column.getLikeVal() + "'";
            } else {
                whereColumnSql = column.getColumnName() + column.getRelationalOperator() + "#{" + column.getParamName() + "}";
            }

            sqlContainer.addWhereColumnPart(whereColumnSql,column);
        }
        return null;
    }

    private String enhanceWhereSql(ColumnDesc[] columns, String whereSql, SqlBuilderContext ctx) {
        SqlContainer sqlContainer = ctx.getSqlContainer();
        for (ColumnDesc column : columns) {
            if(!column.isIgnoreNull() && !column.isMultiValueParam()) {
                continue;
            }

            String columnName = column.getColumnName();
            String paramName = column.getParamName();
            // 从原sql中找到列信息
            EnhanceColumnDesc enhanceColumnDesc = findOrgColumnQuery(columnName, whereSql, 0);
            while (enhanceColumnDesc != null) {
                String enhanceSql;
                if(column.isMultiValueParam()) {
                    enhanceSql = enhanceColumnDesc.connector + sqlContainer.getForeachSql(columnName, paramName, enhanceColumnDesc.queryOperator);
                } else {
                    enhanceSql = whereSql.substring(enhanceColumnDesc.start, enhanceColumnDesc.end);
                }

                enhanceSql = sqlContainer.getIgnoreNullConditionIfNeed(column, enhanceSql);

                // 使用增强sql替换原sql
                whereSql = whereSql.substring(0, enhanceColumnDesc.start)
                        + enhanceSql
                        + (enhanceColumnDesc.end < whereSql.length() ? whereSql.substring(enhanceColumnDesc.end) : "");

                // 继续处理该属性
                //最后一个参数 (whereSql.substring(0,enhanceColumnDesc.start).length+enhanceSql.length+1)=(start-1)+enhanceSql.length+1
                enhanceColumnDesc = findOrgColumnQuery(columnName, whereSql, enhanceColumnDesc.start + enhanceSql.length());
            }
        }

        return whereSql;
    }

    private static final Pattern GROUP_ORDER_PATTERN = Pattern.compile("\\s+(group|order)\\s+by.+", Pattern.CASE_INSENSITIVE);
    private String extractGroupOrderSql(String whereSql, SqlBuilderContext ctx) {
        Matcher matcher = GROUP_ORDER_PATTERN.matcher(whereSql);
        if (matcher.find()) {
            String groupOrderSql = whereSql.substring(matcher.start());

            // 找到order by
            int orderIndex = groupOrderSql.indexOf("order");
            if(orderIndex > 0) {
                ctx.getSqlContainer().setOrderBySql(groupOrderSql.substring(orderIndex));
                if(groupOrderSql.substring(0, orderIndex).indexOf("group") > 0) {   // 再找group by
                    ctx.getSqlContainer().setGroupBySql(groupOrderSql.substring(0, orderIndex));
                }
            } else {    // 找到group by
                ctx.getSqlContainer().setGroupBySql(groupOrderSql);
            }

            return whereSql.substring(0, matcher.start());
        }
        return whereSql;
    }


    private static final Pattern NOT_IN_PATTERN = Pattern.compile("not\\s+in", Pattern.CASE_INSENSITIVE);
    // 找到fieldName，再找到成对的{}或''
    private EnhanceColumnDesc findOrgColumnQuery(String columnName, String whereSql, int fromIndex) {
        if(fromIndex >= whereSql.length()) {
            return null;
        }

        whereSql = whereSql.toLowerCase();
        // 找到columnName
        int start = whereSql.indexOf(columnName, fromIndex);
        if(start < 0) {
            return null;
        }

        char[] chars = whereSql.toCharArray();

        int searchType = 0;
        int end = -1;
        for(int i = start; i < chars.length; i++) {
            if(chars[i] == '\'' && searchType == 0) {
                searchType = 1; // 找到成对‘’的开始'
            } else if(chars[i] == '{' && searchType == 0) {  // 找到{
                searchType = 2;
            } else if(chars[i] == '\'' && searchType == 1) {   // 找到成对‘’的结束'
                end = i;
                break;
            } else if(chars[i] == '}' && searchType == 2) {
                end = i;
                break;
            }
        }
        if(end < 0) {
            return null;
        }

        EnhanceColumnDesc result = new EnhanceColumnDesc();

        result.connector = "";
        if(start > 0) {
            int notSpaceIndex = start - 1;  // 向前找，去掉空格数
            while (notSpaceIndex > 0 && Character.isSpaceChar(whereSql.charAt(notSpaceIndex))) {
                notSpaceIndex--;
            }

            // 找到前一个条件连接符and
            int andIndex = whereSql.lastIndexOf("and", start);
            // 找到前一个条件连接符or
            int orIndex = whereSql.lastIndexOf("or", start);
            if(andIndex > 0 && andIndex + 2 == notSpaceIndex) {
                start = notSpaceIndex - 2;
                result.connector = " and ";
            } else if(orIndex > 0 && orIndex + 1 == notSpaceIndex){
                    start = notSpaceIndex - 1;
                result.connector = " or ";
            }
        }
        result.start = start;
        result.end = end + 1;   // end是该参数查询条件后一位

        Matcher notInMatcher = NOT_IN_PATTERN.matcher(whereSql.substring(start, end));
        if(notInMatcher.find()) {
            result.queryOperator = " not in ";
        } else {
            result.queryOperator = " in ";
        }
        return result;
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.UNKNOWN;
    }

    static class EnhanceColumnDesc {
        int start;
        int end;    // 不包括end
        String connector;
        String queryOperator;
    }
}

