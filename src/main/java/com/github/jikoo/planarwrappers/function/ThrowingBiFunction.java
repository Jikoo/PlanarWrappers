package com.github.jikoo.planarwrappers.function;

/**
 * A function accepting two parameters that may throw an exception.
 *
 * @param <T> the type of the first input
 * @param <U> the type of the second input
 * @param <R> the type of the result
 * @param <E> the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> {

  R apply(T t, U u) throws E;
}
