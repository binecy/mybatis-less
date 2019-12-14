package com.binecy.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
    private static Field getField(Object ojb, String fieldName) {
        Class<?> clazz = ojb.getClass();

        while (clazz != null) {
            Field f = null;
            try {
                f = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }

            if(f != null) {
                return f;
            }
            clazz = clazz.getSuperclass();
        }

        throw new MybatisLessException("Error getting Field:" + clazz.getSimpleName() + "." + fieldName);
    }

    public static <T> T getFieldVal(Object ojb, String fieldName, Class<T> returnType) {
        try {
            Field f = getField(ojb, fieldName);

            f.setAccessible(true);
            T t = (T) f.get(ojb);
            f.setAccessible(false);
            return t;
        } catch (MybatisLessException e) {
            throw  e;
        } catch (Exception e) {
            throw new MybatisLessException("Error getting FieldVal:"
                    + ojb.getClass().getSimpleName() + "." + fieldName, e);
        }
    }


    public static void setFieldVal(Object ojb, String fieldName, Object newVal) {
        try {
            Field f = getField(ojb, fieldName);
            f.setAccessible(true);
            f.set(ojb, newVal);
            f.setAccessible(false);
        } catch (MybatisLessException e) {
            throw  e;
        } catch (Exception e) {
            throw new MybatisLessException("Error setting FieldVal:"
                    + ojb.getClass().getName() + "." + fieldName, e);
        }
    }

    public static String getClassName(Class<?> clazz) {
        StringBuilder s = new StringBuilder(clazz.getSimpleName());

        char c =  Character.toLowerCase(s.charAt(0));
        return s.replace(0, 1, String.valueOf(c)).toString();
    }

    public static Object invokeMethod(Object o, String methodName,Object... args) {
        if(o == null)
            return null;

        Method[] methods = o.getClass().getMethods();
        for (Method method : methods) {
            if(method.getName().equals(methodName)) {
                try {
                    return method.invoke(o, args);
                } catch (Exception e) {
                    throw new MybatisLessException("Error invoking method:"
                            + o.getClass().getSimpleName() + "." + methodName, e);
                }
            }
        }

        return null;
    }
}
