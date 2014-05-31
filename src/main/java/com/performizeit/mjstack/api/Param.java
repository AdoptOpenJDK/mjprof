package com.performizeit.mjstack.api;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    Class type() default String.class;
    String value() default "";
    boolean optional() default false;
    String defaultValue() default "";


}
