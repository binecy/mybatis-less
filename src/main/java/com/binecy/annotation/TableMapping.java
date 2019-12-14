package com.binecy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface TableMapping {
  String tableName() default "";


  Class<?> mappingClass() ;

  ColumnMapping[] columnMapping() default {};

  String[] ignoreProperty() default {};
}
