package ru.craftlogic.api.util;

import java.util.Arrays;

public class Reflection {
    public static Class getCallerClass(int level) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String rawFQN = trace[level+1].toString().split("\\(")[0];
        try {
            return Class.forName(rawFQN.substring(0, rawFQN.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void assertCallerClass(int level, Class<?>... allowedClasses) {
        Class callerClass = getCallerClass(level);

        for (Class<?> allowedClass : allowedClasses) {
            if (allowedClass == callerClass) {
                return;
            }
        }
        throw new IllegalStateException("Invalid caller class " + callerClass + " should be one of " + Arrays.toString(allowedClasses));
    }
}
