package com.binecy.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SelectProperty {
  String[] value() default {};
  String id() default "";
}
