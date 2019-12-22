package com.binecy.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface UpdateProperty {
    String id() default "";
    String[] value() default {};
    String[] ignoreNullProperty() default {};
}
