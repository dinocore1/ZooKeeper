package com.devsmart.zookeeper;

import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.concurrent.Callable;

public class DeferredUtil {


    public static Object unpack(Object path) {
        Object current = path;
        while (current != null) {
            if (current instanceof Callable) {
                current = uncheckedCall((Callable) current);
            } else {
                return current;
            }
        }
        return null;
    }

    /**
     * Calls the given callable converting any thrown exception to an unchecked exception
     *
     * @param callable The callable to call
     * @param <T> Callable's return type
     * @return The value returned by {@link Callable#call()}
     */
    public static <T> T uncheckedCall(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new UncheckedExecutionException(e);
        }
    }
}
