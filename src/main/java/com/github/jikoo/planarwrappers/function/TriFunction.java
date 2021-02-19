package com.github.jikoo.planarwrappers.function;

/**
 * An interface accepting three arguments to produce a result.
 *
 * @param <R> the type of the result
 * @param <T> the type of the first input
 * @param <U> the type of the second input
 * @param <V> the type of the third input
 */
@FunctionalInterface
public interface TriFunction<R, T, U, V> {

  R apply(T t, U u, V v);
}
