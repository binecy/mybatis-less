
package com.binecy.mapper;

import com.binecy.annotation.*;
import com.binecy.domain.Subject;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@TableMapping(
        tableName = "subject",
        mappingClass = Subject.class,
        ignoreProperty = {"classify"},
        columnMapping = {
                @ColumnMapping(property = "createTime", column = "cdt")
        })
public interface SubjectMapperForJDK7 {
    // 插入
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertSubject(Subject subject);

    @InsertProperty("id,code,title,author,readCount")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer insertSubjectProperties(Subject subject);

    // 批量插入
//    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer batchInsertSubject(List<Subject> subjects);

//    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @InsertProperty("id,code,title,author,readCount")
    Integer batchInsertSubjectProperties(List<Subject> subjects);

    // 无参数查询
    List<Subject> listAll();

    Subject selectById(@Param("id") long id);

    @SelectProperty("code")
    String getCode(@Param("id") long id);

    List<Subject> selectOnReadCount(@IgnoreNull @Gt @Param("readCountStart") Integer readCountStart,
                                    @IgnoreNull @Lt @Param("readCountEnd") Integer readCountEnd);

    List<Subject> selectOnReadCount2(@GtEq @Param("readCountStart") int readCountStart,
                                     @LtEq @Param("readCountEnd") int readCountEnd);

    List<Subject> selectNotEq(@NotEq @Param("id") long id);

    // 集合参数
    List<Subject> selectByIds(@IgnoreNull @Param("ids") List<Long> ids);

    List<Subject> selectNotInIds(@NotIn @IgnoreNull @Param("ids") long[] ids);

    // like
    List<Subject> selectLikeTitle(@Like("${title}%") @Param("title") String title);

    List<Subject> selectLikeTitle2(@Like @Param("title") String title);

    // group order
    @SelectProperty("sum(read_count) as read_count, author")
    @Order(by = "read_count", desc = true)
    @Group(by = "author", having = "read_count > #{readCountStart}")
    List<Subject> groupByAuthor(@Gt @Param("idBegin") Long idBegin, @Param("readCountStart") int readCountStart);

    // Condition
    @Condition("(id in #{ids} and title = #{title}) or (id not in #{ids} and author = #{author})")
    List<Subject> selectByCond(@Param("ids") List<Long> ids, @Param("title") String title, @Param("author") String author);

    @Condition("id in #{ids}")
    List<Subject> selectByCond2(@IgnoreNull @Param("ids") List<Long> ids);


    @Condition("id in #{ids} or  author = #{author}")
    List<Subject> selectByCond3(@IgnoreNull @Param("ids") List<Long> ids, @IgnoreNull @Param("author") String author);


    @Condition("author = '${author}' and title like '${title}%'")
    List<Subject> selectByCond4(@Param("author") String author, @Param("ids") List<Long> ids,
                                @IgnoreNull @Param("title") String title);


    @Condition("id = #{ids} order by id desc")
    List<Subject> selectByCond5(@IgnoreNull @Param("ids") List<Long> ids);


    @SelectProperty("sum(read_count) as read_count, author")
    @Condition("id = #{ids} group by author order by read_count desc")
    // 注意，必须写出sum(read_count) as read_count，不能sum(read_count) as readCount，应该解析时可能转下划线了，
    List<Subject> selectByCond6(@Param("ids") List<Long> ids);

    @SelectProperty("sum(read_count) as read_count, author")
    @Group(by = "author")
    @Order(by = "read_count", desc = true)
    @Condition("id in #{ids}")
    List<Subject> selectByCond7(@IgnoreNull @Param("ids") List<Long> ids, @Param("createTimeStart") Date createTimeStart);

    // page
    List<Subject> page1(int pageSize);

    List<Subject> page2(@Param("pageSize") int pageSize, @Param("pageNum") int pageNum);
    List<Subject> page3(@Gt @Param("id") long id, @Param("pageSize") int pageSize);

    List<Subject> page4(@Gt @Param("id") long id, @Param("author") String author, @Param("pageSize") int pageSize);

    List<Subject> page5(@Param("ids") List<Long> ids, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    int updateFields(@Param("id") long id, @UpdateProperty @IgnoreNull @Param("title") String title,
                     @IgnoreNull @Param("content") String content);

    int updateFields2(@UpdateProperty @Param("title") String title);

    @UpdateProperty(ignoreNullProperty = {"id","code","author","readCount"})
    int updateSubject(@Param("id") long id,@Param("subject") Subject subject);

    @UpdateProperty(value = "title,content",ignoreNullProperty = "title")
    int updateSubject2(@Param("id") long id, @Param("subject") Subject subject);


    int updateSubjects(@Param("subjects") List<Subject> subjects);

    @UpdateProperty("title")
    int updateSubjects2(@Param("subjects") List<Subject> subjects);

    int deleteById(@Param("id") long id);

    int deleteAll();

    @Select("select * from subject where id = #{id}")
    Subject selectById2(long id);
}
