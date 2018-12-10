package ru.craftlogic.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name();
    String[] syntax() default {""};
    String[] aliases() default {};
    String[] permissions() default {};
    int opLevel() default 4;
    boolean serverOnly() default false;
}
