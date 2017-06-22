package com.zeroarst.library;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // on class level
@Retention(RetentionPolicy.RUNTIME) // not needed at runtime
public @interface Callback {
    boolean mandatory() default true;
}