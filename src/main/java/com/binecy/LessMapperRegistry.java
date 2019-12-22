package com.binecy;

import com.binecy.builder.*;
import com.binecy.util.MybatisLessException;
import com.binecy.util.ReflectUtil;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtField;
import org.apache.ibatis.javassist.CtMethod;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class LessMapperRegistry extends MapperRegistry {
    Configuration config;
    Map<Class<?>, MapperProxyFactory<?>> knownLessMappers;

    private static Class<?> builderClass;
    static {
        try {
            // MapperAnnotationBuilder没有提供扩展方法，通过javassist添加逻辑
            // javassist修改类的字节码，必须在修改的类(MapperAnnotationBuilder)Initiallization之前操作
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get("org.apache.ibatis.builder.annotation.MapperAnnotationBuilder");

            // 添加字段LessMapperBuilderPlus
            CtField f = new CtField(pool.get("com.binecy.LessMapperBuilder"), "lessMapperBuilderPlus", cc);
            cc.addField(f);

            // getSqlSourceFromAnnotations方法前添加逻辑
            CtMethod m = cc.getDeclaredMethod("getSqlSourceFromAnnotations");
            m.insertBefore(
                    "org.apache.ibatis.mapping.SqlSource plusSqlSource = lessMapperBuilderPlus.createSqlSourceFromAnnotations($1,$2,$3);" +
                            "if(plusSqlSource != null) {" +
                            " return plusSqlSource ;" +
                            "}");

            // getSqlCommandType方法前添加逻辑
            CtMethod m2 = cc.getDeclaredMethod("getSqlCommandType");
            m2.insertBefore("org.apache.ibatis.mapping.SqlCommandType plusSqlType = lessMapperBuilderPlus.getSqlCommandType($1);" +
                    "if(plusSqlType  != null) {" +
                    " return plusSqlType ;" +
                    "}");
            builderClass = cc.toClass(LessMapperRegistry.class.getClassLoader(), LessMapperRegistry.class.getProtectionDomain());
        } catch (Exception e) {
            throw new MybatisLessException("Error initializing MybatisLessCode", e);
        }
    }
    
    private Map<String, SqlBuilder> methodPreAndSqlBuilderMapper = new HashMap<>();

    private static final String METHOD_PRE_CONFIG = "mybatisLess.processor.methodPrefix.";

    public LessMapperRegistry(Configuration config) {
        super(config);
        this.knownLessMappers = ReflectUtil.getFieldVal(this, "knownMappers", Map.class);
        this.config = config;

        // 默认的sqlBuilder
        methodPreAndSqlBuilderMapper.put("get", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("list", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("query", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("group", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("count", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("select", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("search", new SelectSqlBuilder());
        methodPreAndSqlBuilderMapper.put("update", new UpdateSqlBuilder());
        methodPreAndSqlBuilderMapper.put("modify", new UpdateSqlBuilder());

        methodPreAndSqlBuilderMapper.put("batchUpdate", new BatchUpdateSqlBuilder());
        methodPreAndSqlBuilderMapper.put("save", new InsertSqlBuilder());
        methodPreAndSqlBuilderMapper.put("add", new InsertSqlBuilder());
        methodPreAndSqlBuilderMapper.put("insert", new InsertSqlBuilder());
        methodPreAndSqlBuilderMapper.put("batchInsert", new BatchInsertSqlBuilder());

        methodPreAndSqlBuilderMapper.put("insert", new InsertSqlBuilder());
        methodPreAndSqlBuilderMapper.put("delete", new DeleteSqlBuilder());
        methodPreAndSqlBuilderMapper.put("remove", new DeleteSqlBuilder());
        methodPreAndSqlBuilderMapper.put("page", new PageSqlBuilder());
        methodPreAndSqlBuilderMapper.put("where", new WhereSqlBuilder());

        // 用户自定义的sqlBuilder
        Properties properties = config.getVariables();

        if(properties != null) {
            Set<Object> keySet = properties.keySet();
            for (Object key : keySet) {
                if (key != null && key.toString().startsWith(METHOD_PRE_CONFIG)) {
                    String pre = key.toString().substring(METHOD_PRE_CONFIG.length());
                    methodPreAndSqlBuilderMapper.put(pre, (SqlBuilder) properties.get(key));
                }
            }
        }
    }

    @Override
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;

            knownLessMappers.put(type, new MapperProxyFactory<>(type));
            try {
                Constructor<?> constructor = builderClass.getConstructor(Configuration.class, Class.class);

                MapperAnnotationBuilder mapperAnnotationBuilder = (MapperAnnotationBuilder) constructor.newInstance(config, type);

                // 新增逻辑
                LessMapperBuilder builderPlus = new LessMapperBuilder(config, type, methodPreAndSqlBuilderMapper);
                Field f = builderClass.getDeclaredField("lessMapperBuilderPlus");
                f.setAccessible(true);
                f.set(mapperAnnotationBuilder, builderPlus);
                mapperAnnotationBuilder.parse();

                loadCompleted = true;
            } catch (MybatisLessException e) {
                throw e;
            } catch (Exception e) {
                throw new MybatisLessException("Error addMapper:" + type.getSimpleName(), e);
            } finally {
                if (!loadCompleted) {
                    knownLessMappers.remove(type);
                }
            }
        }
    }
}
