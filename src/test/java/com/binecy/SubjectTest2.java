package com.binecy;

import com.binecy.builder.LogInsertSqlBuilder;
import com.binecy.domain.Subject;
import com.binecy.mapper.SubjectMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.*;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubjectTest2 {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        // create a SqlSessionFactory
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
//            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new LessSqlSessionFactoryBuilder();
            Properties properties = new Properties();

            // 自定义SqlBuilder
            properties.put("mybatisLess.processor.methodPrefix.insert", new LogInsertSqlBuilder());
            sqlSessionFactory =sqlSessionFactoryBuilder.build(reader, null, properties);
        }

    }

    private Subject createSubject(int readCount) {
        Subject a = new Subject();
        String code = UUID.randomUUID().toString().replace("-", "");
        a.setCode(code);
        a.setTitle("title" + readCount);
        a.setContent("content" + readCount);
        a.setAuthor("bin");
        a.setAuthor("bin" + (readCount + 4 ) /5);
        a.setCreateTime(new Date());
        a.setReadCount(readCount);
        return a;
    }

    @Test
    public void insert() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            SubjectMapper mapper = sqlSession.getMapper(SubjectMapper.class);

            for(int i = 1; i <= 5; i++) {
                Subject subject = createSubject(i);
                mapper.insertSubject(subject);
                assertNotNull(subject.getId());
            }

            for(int i = 6; i <= 10; i++)
                mapper.insertSubjectProperties(createSubject(i));

            List<Subject> subjects = new ArrayList<>();
            for(int i = 11; i <= 15; i++) {
                subjects.add(createSubject(i));
            }
            mapper.batchInsertSubject(subjects);

            subjects = new ArrayList<>();
            for(int i = 16; i <= 20; i++) {
                subjects.add(createSubject(i));
            }
            mapper.batchInsertSubjectProperties(subjects);
            assertNotNull(subjects.get(0).getId());
        }
    }


}
