package com.github.jikoo.planarwrappers.lang;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface defining behaviors for a locale provider.
 */
public interface LocaleProvider {

  /**
   * Get the default locale. Note that this should be in lower case!
   *
   * @return the default locale
   */
  @NotNull String getDefaultLocale();

  /**
   * Get an {@link Iterable} containing all {@link Message Messages}.
   *
   * @return the iterable messages
   */
  @NotNull Iterable<@NotNull Message> getMessages();

  /**
   * Get whether the manager should log the existence of missing translations.
   *
   * @return whether the manager should log that a guess section exists
   */
  default boolean isWarnIfGuessExists() {
    return true;
  }

  /**
   * Get the location on disk that a locale is saved.
   *
   * @param locale the locale name
   * @return the file where the locale is to be saved
   */
  @NotNull File getLocaleFile(@NotNull String locale);

  /**
   * Open a stream for a locale's bundled presets.
   *
   * @param locale the locale name
   * @return the resource stream, or null if it does not exist
   */
  @Nullable InputStream getLocaleBundle(@NotNull String locale);

  /**
   * Get the {@link Logger} that should be used by the manager. The manager
   * will prefix all its logs with <code>"[LanguageManager]"</code>.
   *
   * @return the logger
   */
  @NotNull Logger getLogger();

}
