package com.github.jikoo.planarwrappers.function;

/**
 * A function accepting three parameters that may throw an exception.
 *
 * @param <T> the type of the first input
 * @param <U> the type of the second input
 * @param <V> the type of the third input
 * @param <R> the type of the result
 * @param <E> the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowingTriFunction<T, U, V, R, E extends Throwable> {

  R apply(T t, U u, V v) throws E;
}
