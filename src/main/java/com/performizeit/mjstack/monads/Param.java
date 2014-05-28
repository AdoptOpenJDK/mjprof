package com.performizeit.mjstack.monads;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    Class value() default String.class;
    String name() default "";
    boolean optional() default false;


}
