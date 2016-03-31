package com.opencredo.concursus.mapping.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface MethodInvoking<I, O> extends Function<I, O> {

    @SuppressWarnings("unchecked")
    static <T, O> BiFunction<T, Object[], O> invokingInstance(Method method) {
        return invokingInstance((Class<? extends O>) method.getReturnType(), method);
    }

    static <T, O> BiFunction<T, Object[], O> invokingInstance(Class<? extends O> returnType, Method method) {
        return (target, args) -> invokingInstance(returnType, target, method).apply(args);
    }

    @SuppressWarnings("unchecked")
    static <O> Function<Object[], O> invokingInstance(Method method, Object target) {
        return invokingInstance((Class<? extends O>) method.getReturnType(), target, method);
    }

    static <O> Function<Object[], O> invokingInstance(Class<? extends O> returnType, Object target, Method method) {
        return of(args -> returnType.cast(method.invoke(target, args)));
    }

    @SuppressWarnings("unchecked")
    static <O> Function<Object[], O> invokingStatic(Method method) {
        return invokingStatic((Class<? extends O>) method.getReturnType(), method);
    }

    static <O> Function<Object[], O> invokingStatic(Class<? extends O> returnType, Method method) {
        return of(args -> returnType.cast(method.invoke(null, args)));
    }

    static <I, O> Function<I, O> of(MethodInvoking<I, O> invocation) {
        return invocation;
    }

    O invoke(I input) throws IllegalAccessException, InvocationTargetException;

    default O apply(I input) {
        try {
            return invoke(input);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
