
package com.binecy.mapper;

import com.binecy.annotation.*;
import com.binecy.domain.Subject;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@TableMapping(
        tableName = "subject",
        mappingClass = com.binecy.domain.Subject.class,
        ignoreProperty = {"classify"},
        columnMapping = {
                @ColumnMapping(property = "createTime", column = "cdt")
        }
        )
public interface SubjectMapper {
    // 插入
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertSubject(Subject subject);

    @InsertProperty(id="baseProperties", value = "id,code,title,author,readCount")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertSubjectProperties(Subject subject);



    @InsertProperty(id="baseProperties")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertSubjectProperties2(Subject subject);

    // 批量插入
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer batchInsertSubject(List<Subject> subjects);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @InsertProperty(id="baseProperties2", value = "id,code,title,author,readCount")
    Integer batchInsertSubjectProperties(List<Subject> subjects);

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @InsertProperty(id="baseProperties2")
    Integer batchInsertSubjectProperties2(List<Subject> subjects);

    // 无参数查询
    List<Subject> listAll();



    Subject getById(long id);

    @SelectProperty(id = "listProperties", value = "id,code,title,author,readCount")
    Subject selectById(long id);

    @SelectProperty(id = "listProperties")
    Subject selectByCode(String code);

    @SelectProperty("code")
    String getCode(long id);

    List<Subject> selectOnReadCount(@IgnoreNull @Gt Integer readCountStart, @IgnoreNull @Lt Integer readCountEnd);

    List<Subject> selectOnReadCount2(@GtEq int readCountStart, @LtEq int readCountEnd);

    List<Subject> selectNotEq(@NotEq long id);

    // 集合参数
    List<Subject> selectByIds(@IgnoreNull List<Long> ids);

    List<Subject> selectNotInIds(@NotIn @IgnoreNull long[] ids);

    // like
    List<Subject> selectLikeTitle(@Like("${_parameter}%") String title);

    List<Subject> selectLikeTitle2(@Like String title);

    // group order
    @SelectProperty("sum(read_count) as read_count, author")
    @Order(by = "read_count", desc = true)
    @Group(by = "author", having = "read_count > #{readCountStart}")
    List<Subject> groupByAuthor(@Gt Long idBegin, int readCountStart);

    // Condition
    @Condition("(id in #{ids} and title = #{title}) or (id not in #{ids} and author = #{author})")
    List<Subject> selectByCond(List<Long> ids, String title, String author);

    @Condition("id in #{ids}")
    List<Subject> selectByCond2(@IgnoreNull List<Long> ids);


    @Condition("id in #{ids} or  author = #{author}")
    List<Subject> selectByCond3(@IgnoreNull List<Long> ids , @IgnoreNull String author);


    @Condition("author = '${author}' and title like '${title}%'")
    List<Subject> selectByCond4(String author, List<Long> ids, @IgnoreNull String title);


    @Condition("id = #{ids} order by id desc")
    List<Subject> selectByCond5(@IgnoreNull List<Long> ids);


    @SelectProperty("sum(read_count) as read_count, author")
    @Condition("id = #{ids} group by author order by read_count desc")
    // 注意，必须写出sum(read_count) as read_count，不能sum(read_count) as readCount，应该解析时可能转下划线了，
    List<Subject> selectByCond6(List<Long> ids);

    @SelectProperty("sum(read_count) as read_count, author")
    @Group(by = "author")
    @Order(by = "read_count", desc = true)
    @Condition("id in #{ids}")
    List<Subject> selectByCond7(@IgnoreNull List<Long> ids, Date createTimeStart);

    // page
    List<Subject> page1(int pageSize);

    List<Subject> page2(int pageSize, int pageNum);
    List<Subject> page3(@Gt long id, int pageSize);

    List<Subject> page4(@Gt long id, String author, int pageSize);

    List<Subject> page5(List<Long> ids, int pageNum, int pageSize);

    int updateFields(long id, @UpdateProperty @IgnoreNull String title, @IgnoreNull String content);

    int updateFields2(@UpdateProperty String title);

    @UpdateProperty(ignoreNullProperty = {"id","code","author","readCount"})
    int updateSubject(long id, Subject subject);

    @UpdateProperty(id = "baseProperties", value = "title,content",ignoreNullProperty = "title")
    int updateSubject2(long id, Subject subject);

    @UpdateProperty(id = "baseProperties")
    int updateSubject3(String code, Subject subject);

    int updateSubjects(List<Subject> subjects);

    @UpdateProperty("title")
    int updateSubjects2(List<Subject> subjects);

    int deleteById(long id);

    int deleteAll();

    @Select("select * from subject where id = #{id}")
    Subject selectById2(long id);
}
