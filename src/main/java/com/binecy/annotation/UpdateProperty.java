package com.binecy.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface UpdateProperty {
    String[] value() default {};
    String[] ignoreNullProperty() default {};
}
