package com.github.jikoo.planarwrappers.function;

/**
 * An interface accepting three arguments to produce a result.
 *
 * @param <T> the type of the first input
 * @param <U> the type of the second input
 * @param <V> the type of the third input
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

  R apply(T t, U u, V v);
}
