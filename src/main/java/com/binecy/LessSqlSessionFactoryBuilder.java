package com.binecy;

import com.binecy.util.ReflectUtil;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

public class LessSqlSessionFactoryBuilder extends SqlSessionFactoryBuilder {
    private static final Log log = LogFactory.getLog(LessSqlSessionFactoryBuilder.class);

    public LessSqlSessionFactoryBuilder() {
        // 主要是为了提前触发LessMapperRegistry中的javassist操作
        log.trace(LessMapperRegistry.class.getName() + " initialized");
    }

    @Override
    public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
            // 获取BaseBuilder.configuration
            Configuration configuration = ReflectUtil.getFieldVal(parser, "configuration", Configuration.class);

            // 更新Configuration.mapperRegistry
            LessMapperRegistry registry = new LessMapperRegistry(configuration);
            ReflectUtil.setFieldVal(configuration, "mapperRegistry", registry);

            SqlSessionFactory result = super.build(parser.parse());

            return result;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                reader.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
        try {
            XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
            // 获取BaseBuilder.configuration
            Configuration configuration = ReflectUtil.getFieldVal(parser, "configuration", Configuration.class);

            // 更新Configuration.mapperRegistry
            LessMapperRegistry registry = new LessMapperRegistry(configuration);
            ReflectUtil.setFieldVal(configuration, "mapperRegistry", registry);

            return super.build(parser.parse());
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error building SqlSession.", e);
        } finally {
            ErrorContext.instance().reset();
            try {
                inputStream.close();
            } catch (IOException e) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

    public SqlSessionFactory build(Configuration config) {
        // 更新Configuration.mapperRegistry
        LessMapperRegistry registry = new LessMapperRegistry(config);
        ReflectUtil.setFieldVal(config, "mapperRegistry", registry);

        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(config);

        return sqlSessionFactory;
    }
}
