package com.github.jikoo.planarwrappers.function;

/**
 * A function that may throw an exception.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {

  R apply(T t) throws E;
}
