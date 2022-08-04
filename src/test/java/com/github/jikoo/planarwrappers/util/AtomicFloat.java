package com.github.jikoo.planarwrappers.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple AtomicFloat implementation piggybacking AtomicInteger.
 */
public class AtomicFloat extends Number {

  private final AtomicInteger bits = new AtomicInteger();

  public void set(float value) {
    bits.set(Float.floatToIntBits(value));
  }

  public float get() {
    return Float.intBitsToFloat(bits.get());
  }

  @Override
  public int intValue() {
    return (int) get();
  }

  @Override
  public long longValue() {
    return (long) get();
  }

  @Override
  public float floatValue() {
    return get();
  }

  @Override
  public double doubleValue() {
    return get();
  }
}
