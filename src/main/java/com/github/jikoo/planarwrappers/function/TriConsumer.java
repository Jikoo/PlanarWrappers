package com.github.jikoo.planarwrappers.function;

/**
 * An interface accepting three arguments.
 *
 * @param <T> the type of the first input
 * @param <U> the type of the second input
 * @param <V> the type of the third input
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {

  void accept(T t, U u, V v);
}
