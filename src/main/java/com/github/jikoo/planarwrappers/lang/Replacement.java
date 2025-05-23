package com.github.jikoo.planarwrappers.lang;

/**
 * A data holder for string replacement in translations.
 */
public interface Replacement {

  /**
   * Get the identifier of the placeholder to be replaced,
   * i.e. {@code "$user"} or {@code "{false}"}.
   *
   * @return the identifier of the placeholder
   */
  String getPlaceholder();

  /**
   * Get the value to replace the placeholder with.
   *
   * @return the placeholder replacement
   */
  String getValue();

}
