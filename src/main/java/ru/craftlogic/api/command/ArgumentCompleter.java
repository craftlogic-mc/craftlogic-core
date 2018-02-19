package ru.craftlogic.api.command;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentCompleter {
    String type();
    boolean isEntityName() default false;
}
