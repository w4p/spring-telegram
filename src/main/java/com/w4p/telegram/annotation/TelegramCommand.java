package com.w4p.telegram.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TelegramCommand {
    String[] value() default {};
    String description() default "";

    boolean hidden() default false;
    boolean isHelp() default false;
}
