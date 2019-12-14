package com.binecy.util;

import java.lang.annotation.Annotation;

// jdk8之前没有这个类
public class Parameter {

    private Annotation[] annotations;
    private Class<?> type;
    private int index;
    private String name;

    public Parameter(Annotation[] annotations, Class<?> type, int index) {
        this.annotations = annotations;
        this.type = type;
        this.index = index;
    }


    public Parameter(Annotation[] annotations, Class<?> type, String name) {
        this.annotations = annotations;
        this.type = type;
        this.name = name;
    }
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                if(annotationClass.isAssignableFrom(annotation.getClass())) {
                    return (T) annotation;
                }
            }
        }
        return null;
    }

    public Class<?> getType() {
        return type;
    }

    // mybatis以1开始
    public String getName() {
        if (name != null) {
            return name;
        }
        return "param" + (index + 1);
    }
}
