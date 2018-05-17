package ru.craftlogic.api.util;

import java.util.Arrays;

public class Reflection {
    public static Class getCallerClass(int level) throws ClassNotFoundException {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String rawFQN = stElements[level+1].toString().split("\\(")[0];
        return Class.forName(rawFQN.substring(0, rawFQN.lastIndexOf('.')));
    }

    public static void assertCallerClass(int level, Class<?>... allowedClasses) {
        Class callerClass = null;
        try {
            callerClass = getCallerClass(level);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (Class<?> allowedClass : allowedClasses) {
            if (allowedClass == callerClass) {
                return;
            }
        }
        throw new IllegalStateException("Invalid caller class " + callerClass + " should be one of " + Arrays.toString(allowedClasses));
    }
}
