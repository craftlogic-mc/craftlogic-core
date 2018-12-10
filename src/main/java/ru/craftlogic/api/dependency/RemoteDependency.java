package ru.craftlogic.api.dependency;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RemoteDependencies.class)
public @interface RemoteDependency {
    String value();
    String[] mirrors() default {};
    String[] transformerExclusions() default {};
}
