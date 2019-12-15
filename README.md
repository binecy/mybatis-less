# Mybatis-Less

Mybatis-Less是一个基于Mybatis开发的插件，支持Mybatis-3.3及以上版本。  
Mybatis-Less可以根据Mapper接口的方法注解自动生成动态SQL，同时支持使用Mybatis注解注入SQL，方便我们编写复杂SQL。  


## 特性

**为什么写这个插件**  
先说一下为什么写这个插件。  
我们喜欢Mybatis，因为它灵活，可以自行编写各种SQL，满足我们复杂的业务逻辑。  
但使用Mybatis，要编写很多简单的，重复的SQL。  
如果可以自动生成这些简单SQL，就可以减少大量不必要的工作。  
同时还需要支持使用Mybatis注解注入SQL，方便我们根据需要编写复杂SQL。  
于是，Mybatis-Less就应运而生了。  

### 入门简单
基本上会使用Mybatis，就可以Mybatis-Less了。  
只是添加了几个简单的注解，就可以生成动态SQL了，没有过多学习成本。

### 兼容Mybatis
Mybatis-Less只是对Mybatis进行了功能扩展，不会影响Mybatis原有的功能和正常使用。

Mybatis-Less完全兼容Mybatis的功能，一个Mapper接口中可以同时定义Mybatis-Less生成SQL的方法和Mybatis注入SQL的方法，
甚至Mybatis-Less生成SQL的方法也可以使用Mybatis的@Options，@Results等注解。  
这是现在Mybatis插件很少做到的。

### 实现简单，扩展性强
实现代码总共3000多行，非常简单，而且用户可以根据需要自行扩展。

### 性能
Mybatis-Less会在应用启动时生成动态SQL，不会对应用运行性能造成影响。

* [功能](#功能)
    * [方法前缀](#方法前缀)
	* [插入](#插入)
	    * [批量插入](#批量插入)
	* [查询](#查询)
	    * [关系运算符](#关系运算符)
	    * [忽略NULL参数](#忽略NULL参数)
	    * [排序](#排序)
	    * [分组](#分组)
	* [更新](#更新)
	    * [更新属性](#更新属性)
	    * [更新实体](#更新实体)
	    * [批量更新](#批量更新)
	* [删除](#删除)
	* [Condition](#condition)
	* [分页](#分页)
* [使用](#使用)
    * [@TableMapping](#@TableMapping)
    * [启动LessSqlSessionFactoryBuilder](#启动LessSqlSessionFactoryBuilder)
    * [支持Spring-Boot](#支持Spring-Boot)
    * [@Param](#@Param)
    * [兼容mybatis](#兼容mybatis-1)
    * [扩展](#扩展)
    * [关闭驼峰转化下划线开关](#关闭驼峰转化下划线开关)
    * [注解总结](#注解总结)

## 功能
我们通过一个简单的例子来介绍Mybatis-Less的功能。

定义一个实体映射类Subject
```java
public class Subject {
    private Long id;
    private String title;
    private String content;
    private String author;
    private Long readCount;
    private Date createTime;

    // ... getter and setter
}    
```
对应的表结构
```SQL
CREATE TABLE subject (
  id      INT NOT NULL primary key auto_increment,
  title   VARCHAR(64),
  content VARCHAR(1024),
  author  VARCHAR(16),
  read_count INT,
  create_time  TIMESTAMP
);
```

### 方法前缀
Mybatis-Less根据接口方法名的前缀生成不同操作类型的动态SQL：

|             方法前缀             | SQL操作 |
|----------------------------------|---------|
| save,insert,add                  | 插入/批量插入  |
| select,get,list,count,page,query,search | 查询  |
| update,modify                    | 更新/批量更新  |
| delete,remove                    | 删除  |
| batchInsert                    | 批量插入  |
| batchUpdate                    | 批量更新  |

### 插入
要插入一个Subject对象，只要在Mapper接口中定义如下方法
```
---> java方法
Integer insertSubject(Subject subject);

---> 动态SQL
insert into subject(id,title,content,author,read_count,create_time)
values (#{id},#{title},#{content},#{author},#{readCount},#{createTime})
```
下面就是Mybatis-Less生成的动态SQL。

Mybatis-Less支持与Mybatis的@Options注解共用，如果要使用mySQL的自增id，可以添加@Options注解
```
@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
Integer insertSubject(Subject subject);
```

如果我们只是想插入映射类中的部分字段，可以使用@InsertProperty注解标明需插入的字段
```
@InsertProperty("id,title,content")
@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
Integer insertSubject2(Subject subject);
```

#### 批量插入
*批量插入目前只支持mySQL数据库*
如果要批量插入数据，只需定义如下方法
```
Integer insertSubjects(List<Subject> subjects);
```
批量插入同样支持@Options，@InsertProperty等注解

### 查询
最简单的，通过id查询
```
---> java方法
Subject selectById(long id);

---> 动态SQL
select * from subject <where>id= #{id}</where>
```

Mybatis-Less支持集合，数组参数查询
```
---> java方法
List<Subject> selectByIds(List<Long> ids);

---> 动态SQL
select * from subject
<where>
    id in <foreach collection='collection' item='item' open='(' close=') ' separator=','>#{item}</foreach>
</where>
```
*注意：参数名不必与属性名完成相同，但必须以字段名开头，如上例中的ids。*

#### 关系运算符
查询条件中的关系运算符默认为=，集合参数的关系运算符默认为in，可以通过以下参数注解修改关系运算符

| 参数注解 | 运算符 |
|----------|--------|
| @Gt      | >      |
| @Lt      | <      |
| @LtEq    | >=     |
| @LtEq    | <=     |
| @NotEq   | <>     |
| @Like    | like   |
| @NotIn   | not in |

*注意：关系运算符注解表示数据库列与查询参数的对比。如@Gt表示列大于查询参数。*

使用关系运算符注解，按时间范围查询
```
---> java方法
List<Subject> selectOnTime(@Gt Date cdtStart, @Lt Date cdtEnd);

---> 动态SQL
select * from subject
<where>
    create_time > #{createTimeStart} and create_time < #{createTimeEnd}
</where>
```

@Like支持使用Mybatis的${}格式占位符，如查询以hello开头的主题
```
---> java方法
List<Subject> selectLikeTitle(@Like("${title}%") String title);

---> 动态SQL
select * from subject <where>title like '${title}%'</where>
```

#### 忽略NULL参数
@IgnoreNull注解表示，如果该参数为null，则不作为查询条件
```
---> java方法
List<Subject> select(@IgnoreNull Long id, @IgnoreNull String title);

---> 动态SQL
select * from subject
<where>
    <if test='id != null'>id= #{id}</if>
    <if test='title != null'> and title= #{title}</if>
</where>
```

#### 排序
可以使用@Order注解进行排序
```
@Order(by = "create_time", desc = true)
List<Subject> selectByIds(List<Long> ids);
```
desc为true表示倒序

#### 分组
可以使用@Group指定分组的列以及having条件
```
@Group(by = "author", having = "sum(read_count) > readCountStart")
List<Subject> groupByAuthor(@Gt Date createTimeStart,@InHaving int readCountStart);

---> 动态SQL
select * from subject
<where>
	create_time > #{createTimeStart}
</where>
group by author having sum(read_count) > #{readCountStart}
```
*注意：用于having条件的参数不再生成where条件。*

### 更新
更新操作可以更新属性或者更新实体。
#### 更新属性
```
---> java方法
int updateProperties(long id, @UpdateProperty @IgnoreNull String title, @IgnoreNull String content);

---> 动态SQL
update subject
<set>
    <if test='title != null'>title=#{title},</if>
    <if test='content != null'>content=#{content}</if>
</set>
<where>
	id= #{id}
</where>
```
*注意：必须遵循约定，用于查询的参数在前，用于更新的参数在后（参数必须以属性名开头），中间以@UpdateProperty分隔。*  
@IgnoreNull注解表示该参数为null，则不更新该列。

#### 更新实体
```
---> java方法
int updateSubject(long id, Subject subject);

---> 动态SQL
update subject
<set>
    id=#{subject.id},
    title=#{subject.title},
    content=#{subject.content},
    author=#{subject.author},
    read_count=#{subject.readCount},
    create_time=#{subject.createTime}
</set>
<where> id= #{id} </where>
```
*注意：同样要遵循约定，用于查询的参数在前，用于更新的实体类参数在最后。*  
如果只更新一部分字段，可以使用UpdateProperty注解标示需更新的字段和ignoreNull的字段
```
@UpdateProperty(value = "title,content", ignoreNullProperty = "content")
int updateSubject(long id, Subject subject);
```

#### 批量更新
*批量更新只支持mySQL数据库，可以通过id或者唯一索引的字段进行批量更新操作。*
```
---> java方法
int updateSubjects(List<Subject> subjects);

---> 动态SQL
update subject
<set>
    id=<foreach collection='collection' item='subject' index='index' separator=' ' open='case id ' close=' end'>
        when #{subject.id} then #{subject.id}
    </foreach>,
    ...
</set>
<where>
    id in  <foreach collection='collection' index='index' item='subject'   separator=',' open='(' close=')'>
        #{subject.id}
    </foreach>
</where>
```
为了节省篇幅，不展示完整的动态SQL。  
批量更新操作同样支持@UpdateProperty注解。  
上面例子中，id用于查询条件和更新属性的定位条件，也可以使用@BatchUpdateKey指定该属性。


### 删除
删除比较简单，
```
---> java方法
int deleteById(long id);

---> 动态SQL
delete from subject <where> id= #{id} </where>
```
delete操作比较简单，删除条件参数与select操作类似，这里不过多重复。

### @Condition
select操作和delete操作可以使用@Condition注解编写where条件，Mybatis-Less会根据该条件生成动态SQL。
```
---> java方法
@Condition("id = #{id} or title like '${title}%'")
List<Subject> selectByCond(Long id, String title);

---> 动态SQL
select * from subject <where>id = #{id} or title like '${title}%'</where>
```

@Condition注解可以与@IgnoreNull注解同时使用
```
---> java方法
@Condition("id = #{id} or title like '${title}%'")
List<Subject> selectByCond(@IgnoreNull Long id, @IgnoreNull String title);

---> 动态SQL
select content,title,read_count,id,author,cdt,code from subject
<where>
    <if test='id != null'>id = #{id}</if>
    <if test='title != null'>or title like '${title}%'</if>
</where>
```

@Condition注解也同样支持数组，集合参数
```
---> java方法
@Condition("id in #{ids} or author not in #{authors}")
List<Subject> selectByCond2(List<Long> ids, List<String> author);

---> 动态SQL
select content,title,read_count,id,author,cdt,code from subject
<where>
    id in <foreach collection='ids' item='item' open='(' close=') ' separator=','>#{item}</foreach>
    or author not in <foreach collection='authors' item='item' open='(' close=') ' separator=','>#{item}</foreach>
</where>
```

支持同时使用@Order和@Group注解
```
---> java方法
@Condition("create_time > #{createTimeStart}")
@Group(by = "author", having = "sum(read_count) > #{readCountStart}")
List<Subject> groupByAuthor(Date createTimeStart, int readCountStart);

---> 动态SQL
select content,title,read_count,id,author,cdt,code from subject
<where>
    create_time > #{createTimeStart}
</where>
group by author having sum(read_count) > #{readCountStart}
```
也可以将order，group条件写入到@Condition条件中
```
@Condition("create_time > #{createTimeStart} group by author having sum(read_count) > #{readCountStart}")
List<Subject> groupByAuthor(Date createTimeStart, int readCountStart);
```

### 分页
**分页操作仅支持mySQL数据库**  
遵循约定，方法需提供参数pageNum，pageSize表示页码和每页数量，而且这两个参数必须位于方法参数最后两位。  
如果只查询第一页数据，可以不提供pageNum参数，但pageSize参数必须提供。

有两种方法分页  
1.使用pageNum，pageSize参数查询指定页码数据
```
---> java方法
List<Subject> page(int pageNum, int pageSize);

---> 动态SQL
<bind name='pageOffset' value='(pageNum-1)*pageSize' />
select * from subject  limit #{pageOffset}, #{pageSize}
```
使用Mybatis的bind语法计算数据偏移量

2.对于id自增的表，如果可以回传上一页最后一条数据的id，则可以根据id查询
```
---> java方法
List<Subject> pageById(@Gt long id, int pageSize);

---> 动态SQL
select * from subject <where>id > #{id}</where> limit #{pageSize}
```
*注意：pageNum从1开始。*

## 使用
### @TableMapping
@TableMapping标明Mapper接口和数据表的对应关系。  
Mapper接口必须存在这注解，Mybatis-Less才为该Mapper接口的生成SQL。  
其中tableName属性指定该Mapper接口对应的表名，不指定则使用Mapper接口名称转为下划线格式的字符串，  
mappingClass属性指定表对于的实体类。

如果实体属性名和表列名没有对应，可以使用@columnMapping映射属性名和列名
```java
@TableMapping(
        tableName = "subject",
        mappingClass = com.binecy.domain.Subject.class,
        columnMapping = {
                @columnMapping(field = "cdt", column = "create_time")
        })
public interface SubjectMapper {
	...
}
```

### 启动LessSQLSessionFactoryBuilder
要使用Mybatis-Less，很简单，只要把SQLSessionFactoryBuilder换成LessSQLSessionFactoryBuilder就可以了
```java
    Reader reader = Resources.getResourceAsReader("Mybatis-config.xml");
    SQLSessionFactoryBuilder SQLSessionFactoryBuilder = new LessSQLSessionFactoryBuilder();
    SQLSessionFactory SQLSessionFactory =SQLSessionFactoryBuilder.build(reader);
```


### 支持Spring-Boot
在Spring-Boot中使用Mybatis-Less，最简单配置如下
```java
    @Bean
    public SQLSessionFactory SQLSessionFactory(DataSource dataSource) throws Exception {
        SQLSessionFactoryBean factory = new SQLSessionFactoryBean();
        factory.setSQLSessionFactoryBuilder(new LessSQLSessionFactoryBuilder());
        factory.setDataSource(dataSource);
        return factory.getObject();
    }
```


### @Param
如果使用JDK8+，Mybatis-3.5+, 并且使用`'-parameters'`编译代码，可以不用@Param注解标注参数（推荐使用这种方法）。  
否则必须要使用@Param标注参数名。  
*注意：原来的Mybatis的#{param1}, #{param2}的默认参数名不可以使用。*

### 兼容Mybatis
在一个Mapper接口中，可以同时定义Mybatis-Less生成SQL方法和Mybatis注入SQL的方法
```java
public interface SubjectMapper {
    // Mybatis-Less生成sql
    Subject selectById(long id);

    // Mybatis注入SQL
    @Select("select * from subject where id = #{id}")
    Subject selectByMybatis(long id);
}
```
如果使用了Mybatis注入SQL的注解(Select/Insert/Update/Delete/SelectProvider/InsertProvider/UpdateProvider/DeleteProvider)，Mybatis-Less则不会为该方法生成SQL。

### 扩展
SQLSessionFactoryBuilder.build方法的Properties参数可以传入用户定义的属性，Mybatis-Less从该properties中获取用户配置。  
如果开发者想添加方法前缀及SQL构建器，可以添加
```java
properties.put("mybatisLess.processor.methodPrefix.alter",  new AlterSQLBuilder());
```
alter为方法前缀，AlterSQLBuilder需要实现SQLBuilder接口，构建动态SQL。
默认SQLBuilder接口：
* UpdateSQLBuilder负责构建Update SQL
* InsertSQLBuilder负责构建Insert SQL
* SelectSQLBuilder负责构建Select SQL
* DeleteSQLBuilder负责构建Delete SQL

### 关闭驼峰转化下划线开关
Mybatis-Less默认将实体属性的驼峰格式转化为表列的下划线格式，可以通过配置Mybatis.less.mapper.toUnderscore关闭。
```
properties.put("mybatisLess.mapping.toUnderLine", "false");
```

### 注解总结
注解总结如下

| 注解           | 使用对象 |描述   |
|--|--|--|
| @TableMapping  | 类       | Mybatis-Less的标示注解，属性：TableMapping指定映射类，tableName指定表名，columnMapping指定列和属性的映射 |
| @ColumnMapping |          | 指定单一表列名和类字段名的映射  |
| @InsertProperty   | 方法     | 指定insert的字段 |
| @UpdateProperty   | 方法     | 指定 update的字段,属性：ignoreNullProperty指定更新时忽略null的字段 |
| @SelectProperty   | 方法     | 指定 select的字段 |
| @BatchUpdateKey   | 方法     | 批量更新时，指定用于查询和更新的字段 |
| @Condition | 方法 | 编写查询SQL |
| @IgnoreNull | 参数 | 如果该参数为null，不作为查询条件 |
| @Gt | 参数 | 关系运算符 > |
| @GtEq | 参数 | 关系运算符 >= |
| @Lt | 参数 | 关系运算符 < |
| @LtEq | 参数 | 关系运算符 <= |
| @NotEq | 参数 | 关系运算符 <> |
| @NotIn | 参数 | 关系运算符 not in |
| @Like | 参数 | 关系运算符 like，属性value：支持Mybatis ${}占位符 |
| @Order | 方法 | 指定排序字段，属性by：排序字段，desc：true表示倒序 |
| @Group | 方法 | 指定分组字段，属性by：分组字段，having：编写having条件 |

## todo
批量操作，分页支持其他数据库
