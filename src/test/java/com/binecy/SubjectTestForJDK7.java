package com.binecy;

import com.binecy.LessSqlSessionFactoryBuilder;
import com.binecy.domain.Subject;
import com.binecy.mapper.SubjectMapperForJDK7;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.*;

import static org.junit.Assert.*;

public class SubjectTestForJDK7 {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {

        // create a SqlSessionFactory
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
//            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
            SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new LessSqlSessionFactoryBuilder();
            Properties properties = new Properties();

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

            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);


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

            sqlSession.commit();
        }
    }


    @Test
    public void select() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);
            List<Subject> allData = mapper.listAll();
            Subject first = allData.get(0);
            Subject second = allData.get(1);
            Subject third = allData.get(2);

            Subject s = mapper.selectById(first.getId());
            assertEquals(s.getTitle(), first.getTitle());

            String c = mapper.getCode(first.getId());
            assertEquals(first.getCode() , c);

            List<Subject> rs = mapper.selectOnReadCount(5,10);
            assertEquals(rs.size(), 4);

            rs = mapper.selectOnReadCount(5,null);
            assertEquals(rs.size(), 15);

            rs = mapper.selectOnReadCount(null,10);
            assertEquals(rs.size(), 9);

            rs = mapper.selectOnReadCount(null,null);
            assertEquals(rs.size(),20);

            rs = mapper.selectOnReadCount2(5,10);
            assertEquals(rs.size(), 6);

            rs = mapper.selectNotEq(first.getId());
            assertEquals(rs.size(), 19);

            rs = mapper.selectByIds(Arrays.asList(first.getId(), second.getId(), third.getId()));
            assertEquals(rs.size(), 3);

            rs = mapper.selectNotInIds(new long[]{first.getId(), second.getId(), third.getId()});
            assertEquals(rs.size(), 17);

            rs = mapper.selectLikeTitle("title");
            assertEquals(rs.size(), 20);

            rs = mapper.selectLikeTitle2("title");
            assertEquals(rs.size(), 0);


            rs = mapper.groupByAuthor(allData.get(4).getId(), 40);
            assertEquals(rs.size(), 2);
            assertTrue(rs.get(0).getReadCount() > rs.get(1).getReadCount());

            rs = mapper.selectByCond(Arrays.asList(first.getId(), second.getId(), third.getId()), first.getTitle(), second.getAuthor());
            assertEquals(rs.size(), 3);
            List<Long> testIds = Arrays.asList(allData.get(3).getId(), allData.get(4).getId(), first.getId());
            assertTrue(testIds.contains(rs.get(0).getId()));
            assertTrue(testIds.contains(rs.get(1).getId()));
            assertTrue(testIds.contains(rs.get(2).getId()));


            rs = mapper.selectByCond2(testIds);
            assertEquals(rs.size(), 3);
            assertTrue(testIds.contains(rs.get(0).getId()));
            assertTrue(testIds.contains(rs.get(1).getId()));
            assertTrue(testIds.contains(rs.get(2).getId()));

            rs = mapper.selectByCond2(null);
            assertEquals(rs.size(), 20);

            rs = mapper.selectByCond3(testIds, first.getAuthor());
            assertEquals(rs.size(), 5);


            rs = mapper.selectByCond3(null, first.getAuthor());
            assertEquals(rs.size(), 5);

            rs = mapper.selectByCond3(testIds, null);
            assertEquals(rs.size(), 3);

            rs = mapper.selectByCond4(first.getAuthor(), testIds, first.getTitle());
            assertEquals(rs.size(), 1);

            rs = mapper.selectByCond4(first.getAuthor(), testIds, null);
            assertEquals(rs.size(), 5);

            rs = mapper.selectByCond5(testIds);
            assertEquals(rs.size(), 3);
            assertTrue(rs.get(0).getId() > rs.get(1).getId());
            assertTrue(rs.get(1).getId() > rs.get(2).getId());

            rs = mapper.selectByCond5(null);
            assertEquals(rs.size(), 20);
            assertTrue(rs.get(0).getId() > rs.get(1).getId());
            assertTrue(rs.get(1).getId() > rs.get(2).getId());

            testIds = new ArrayList<>();
            for(int i = 0; i < 15; i++)
                testIds.add(allData.get(i).getId());
            rs = mapper.selectByCond6(testIds);
            assertEquals(rs.size(), 3);
            assertTrue(rs.get(0).getReadCount() > rs.get(1).getReadCount());
            assertTrue(rs.get(1).getReadCount() > rs.get(2).getReadCount());

            rs = mapper.selectByCond7(testIds, null);
            assertEquals(rs.size(), 3);
            assertTrue(rs.get(0).getReadCount() > rs.get(1).getReadCount());
            assertTrue(rs.get(1).getReadCount() > rs.get(2).getReadCount());

            rs = mapper.selectByCond7(null, null);
            assertEquals(rs.size(), 4);
            assertTrue(rs.get(0).getReadCount() > rs.get(1).getReadCount());
            assertTrue(rs.get(1).getReadCount() > rs.get(2).getReadCount());
        }
    }

    @Test
    public void page() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);
            List<Subject> all = mapper.listAll();
            Subject first = all.get(0);

            List<Subject> rs = mapper.page1(5);
            assertEquals(rs.size(), 5);

            assertTrue(rs.get(0).getId().equals(first.getId()));

            rs = mapper.page2(5, 2);
            assertEquals(rs.size(), 5);
            assertTrue(rs.get(0).getId().equals(all.get(5).getId()));

            rs = mapper.page2(5, 3);
            assertEquals(rs.size(), 5);
            assertTrue(rs.get(0).getId().equals(all.get(10).getId()));

            rs = mapper.page3(all.get(4).getId(), 5);
            assertEquals(rs.size(), 5);
            assertTrue(rs.get(0).getId().equals(all.get(5).getId()));

            rs = mapper.page4(all.get(4).getId(), all.get(10).getAuthor(), 5);
            assertEquals(rs.size(), 5);
            assertTrue(rs.get(0).getId().equals(all.get(10).getId()));


            List<Long> ids = new ArrayList<>();
            for(int i = 5; i < all.size(); i++) {
                ids.add(all.get(i).getId());
            }
            rs = mapper.page5(ids, 2, 5);
            assertEquals(rs.size(), 5);
            assertTrue(rs.get(0).getId().equals(all.get(10).getId()));
        }
    }

    @Test
    public void update() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);

            List<Subject> all = mapper.listAll();
            Subject first = all.get(0);
            mapper.updateFields(first.getId(), "new title", null);
            mapper.updateFields(first.getId(), null, "new context");

            Subject newFirst = mapper.selectById(first.getId());
            assertTrue(newFirst.getTitle().equals("new title"));
            assertTrue(newFirst.getContent().equals("new context"));

            Subject updateTo = new Subject();
            updateTo.setTitle("new title2");
            updateTo.setContent("new context2");
            mapper.updateSubject(first.getId(), updateTo);
            newFirst = mapper.selectById(first.getId());
            assertTrue(newFirst.getTitle().equals("new title2"));
            assertTrue(newFirst.getContent().equals("new context2"));
            assertTrue(newFirst.getReadCount()== first.getReadCount());


            updateTo = new Subject();
            updateTo.setTitle("new title3");
            updateTo.setContent("new context3");
            updateTo.setReadCount(1000);
            mapper.updateSubject2(first.getId(), updateTo);
            newFirst = mapper.selectById(first.getId());
            assertTrue(newFirst.getTitle().equals("new title3"));
            assertTrue(newFirst.getContent().equals("new context3"));
            assertTrue(newFirst.getReadCount()== first.getReadCount());


            List<Subject> updates = all.subList(1, 3);
            updates.get(0).setTitle("new title4");
            updates.get(1).setTitle("new title4");
            mapper.updateSubjects(updates);
            newFirst = mapper.selectById(all.get(1).getId());
            assertTrue(newFirst.getTitle().equals("new title4"));
            assertTrue(newFirst.getContent().equals(all.get(1).getContent()));

            updates = all.subList(3, 5);
            updates.get(0).setTitle("new title5");
            updates.get(1).setTitle("new title5");
            String beforeContent = updates.get(0).getContent();
            updates.get(0).setContent("new context5");
            updates.get(1).setContent("new context5");
            mapper.updateSubjects2(updates);
            newFirst = mapper.selectById(all.get(3).getId());
            assertTrue(newFirst.getTitle().equals("new title5"));
            assertTrue(newFirst.getContent().equals(beforeContent));

            mapper.updateFields2("new title6");
            newFirst = mapper.selectById(first.getId());
            assertTrue(newFirst.getTitle().equals("new title6"));
        }
    }

    @Test
    public void delete() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);

            List<Subject>  all = mapper.listAll();
            mapper.deleteById(all.get(0).getId());
            all = mapper.listAll();
            assertEquals(all.size(), 19);


            mapper.deleteAll();
            all = mapper.listAll();
            assertEquals(all.size(), 0);
        }
    }

    @Test
    public void mybatis() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            SubjectMapperForJDK7 mapper = sqlSession.getMapper(SubjectMapperForJDK7.class);

            List<Subject>  all = mapper.listAll();
            Subject subject = mapper.selectById2(all.get(0).getId());
            assertTrue(subject.getTitle().equals(all.get(0).getTitle()));
        }
    }
}
