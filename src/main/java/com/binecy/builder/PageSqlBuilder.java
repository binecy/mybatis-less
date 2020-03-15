package com.binecy.builder;

import com.binecy.util.ColumnDesc;
import org.apache.ibatis.mapping.SqlCommandType;

public class PageSqlBuilder implements SqlBuilder {
    @Override
    public String buildSql(SqlBuilderContext ctx) {
        int paramCount = ctx.getParameterCount();
        if(paramCount < 1) {
            return null;
        }

        ColumnDesc pageSize = null;
        ColumnDesc pageNum = null;

        ctx.setWhereParamStart(0);
        if(paramCount == 1) {   // 只要一个参数，作为pageSize
            ColumnDesc[] pageDesc = ctx.getHelper().getColumnFromParam(paramCount - 1, paramCount, ctx);
            pageSize = pageDesc[0];

            ctx.setWhereParamEnd(paramCount - 1);
        } else {
            // 解析最后两个参数
            ColumnDesc[] pageDesc = ctx.getHelper().getColumnFromParam(paramCount - 2, paramCount, ctx);

            for(ColumnDesc columnDesc : pageDesc) {
                if("pageSize".equals(columnDesc.getParamName())) {
                    pageSize = columnDesc;
                }
                if("pageNum".equals(columnDesc.getParamName())) {
                    pageNum = columnDesc;
                }
            }

            if(pageSize == null) {
                return null;
            }
            if(pageNum == null) {
                ctx.setWhereParamEnd(paramCount - 1);
            } else {
                ctx.setWhereParamEnd(paramCount - 2);
            }
        }

        String selectSql = ctx.getSelectSqlBuilder().buildSql(ctx);

        if(pageNum != null) {
            return selectSql.replace("<script>", "<script><bind name='pageOffset' value='(" + pageNum.getParamName() + "-1)*" + pageSize.getParamName() + "' />")
                    .replace("</script>", " limit #{pageOffset}, #{" + pageSize.getParamName() + "}</script>");
        } else {
            return selectSql.replace("</script>", " limit #{" + pageSize.getParamName() + "}</script>");
        }
    }

    @Override
    public SqlCommandType getSqlCommandType() {
        return SqlCommandType.SELECT;
    }
}
