package com.github.jikoo.planarwrappers.lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple language manager supporting user-defined and bundled localizations.
 */
public class LanguageManager {

  private static final String GUESS_PATH = "guess";
  private final @NotNull LocaleProvider provider;
  private final @NotNull Map<String, YamlConfiguration> locales;

  public LanguageManager(@NotNull LocaleProvider provider) {
    this.provider = provider;
    this.locales = new HashMap<>();

    reload();
  }

  /**
   * Get the value of a {@link Message} in a locale.
   *
   * <p>If the message is {@code null}, the default value is returned.
   * <br>If the message is blank, it has been removed by the user, so {@code null} is returned.
   *
   * @param locale the locale
   * @param message the message
   * @return the configured message, the default, or {@code null}
   */
  public @Nullable String getValue(@Nullable String locale, @NotNull Message message) {
    String value = getLocale(
        locale == null ? provider.getDefaultLocale() : locale.toLowerCase(Locale.ENGLISH),
        true
    ).getString(message.key());

    if (value == null) {
      value = message.defaultValue();
    }

    if (value.isBlank()) {
      return null;
    }

    return value;
  }

  /**
   * Get the value of a {@link Message} in a locale.
   *
   * <p>If the message is {@code null}, the default value is returned.
   * <br>If the message is blank, it has been removed by the user, so {@code null} is returned.
   * 
   * <p>Replacements may be other messages. If those messages are expected to contain other
   * replacements that are to be respected, those replacements should come after the message.
   * Replacements are performed in declaration order.
   * <br>Messages used in replacement are not allowed to be blank. Blank messages will be replaced with
   * the default value.
   *
   * @see #getValue(String, Message)
   * @param locale the locale
   * @param message the message
   * @param replacements the replacements to make in the translated string
   * @return the configured message, the default, or {@code null}
   */
  public @Nullable String getValue(
      @Nullable String locale,
      @NotNull Message message,
      Replacement @NotNull ... replacements
  ) {
    String value = getValue(locale, message);

    if (value == null) {
      return null;
    }

    for (Replacement replacement : replacements) {
      if (replacement instanceof Message msg) {
        String replaceWith = getValue(locale, msg);
        if (replaceWith == null) {
          replaceWith = replacement.getValue();
        }
        value = value.replace(replacement.getPlaceholder(), replaceWith);
      } else {
        value = value.replace(replacement.getPlaceholder(), replacement.getValue());
      }
    }

    return value;
  }

  /**
   * Get the value of a {@link Message} in a locale.
   *
   * <p>If the message is {@code null}, the default value is returned.
   * <br>If the message is blank, it has been removed by the user, so {@code null} is returned.
   *
   * @see #getValue(String, Message)
   * @param sender the {@link CommandSender} who will be receiving the message
   * @param message the message
   * @return the configured message, the default, or {@code null}
   */
  public @Nullable String getValue(@NotNull CommandSender sender, @NotNull Message message) {
    return getValue(getLocale(sender), message);
  }

  /**
   * Get the value of a {@link Message} in a locale.
   *
   * <p>If the message is {@code null}, the default value is returned.
   * <br>If the message is blank, it has been removed by the user, so {@code null} is returned.
   *
   * <p>Replacements may be other messages. If those messages are expected to contain other
   * replacements that are to be respected, those replacements should come after the message.
   * Replacements are performed in declaration order.
   * <br>Messages used in replacement are not allowed to be blank. Blank messages will be replaced with
   * the default value.
   *
   * @see #getValue(String, Message)
   * @see #getValue(String, Message, Replacement...)
   * @param sender the {@link CommandSender} who will be receiving the message
   * @param message the message
   * @param replacements the replacements to make in the translated string
   * @return the configured message, the default, or {@code null}
   */
  public @Nullable String getValue(
      @NotNull CommandSender sender,
      @NotNull Message message,
      Replacement @NotNull ... replacements
  ) {
    return getValue(getLocale(sender), message, replacements);
  }

  private @NotNull String getLocale(@NotNull CommandSender sender) {
    if (sender instanceof Player player) {
      return player.getLocale();
    } else {
      return provider.getDefaultLocale();
    }
  }

  public void reload() {
    this.locales.clear();
    getLocale(provider.getDefaultLocale(), true);
  }

  @Contract("_, true -> !null")
  private @Nullable YamlConfiguration getLocale(@NotNull String locale, boolean load) {
    YamlConfiguration loaded = locales.get(locale);
    if (loaded != null) {
      return loaded;
    }

    LocaleLocation localeLoc = localeOrParent(locale, null);

    // If a parent was a better match, check if it is already loaded.
    if (!locale.equals(localeLoc.locale)) {
      loaded = locales.get(localeLoc.locale);
      if (loaded != null) {
        locales.put(locale, loaded);
        return loaded;
      }
    }

    if (!load && !localeLoc.file.exists() && localeLoc.bundled == null) {
      return null;
    }

    // Load locale config from disk and bundled locale defaults.
    YamlConfiguration localeConfig = loadLocale(localeLoc);

    if (!locale.equals(provider.getDefaultLocale())
        && provider.isWarnIfGuessExists()
        && localeConfig.isConfigurationSection(GUESS_PATH)) {
      // Warn that guess section exists. This should run once per language per server restart
      // when accessed by a user to hint to server owners that they can make UX improvements.
      provider.getLogger().info(() -> "[LanguageManager] Missing translations from " + localeLoc.locale
          + ".yml! Check the guess section!");
    }

    locales.put(locale, localeConfig);
    locales.put(localeLoc.locale, localeConfig);

    return localeConfig;
  }

  private @NotNull LocaleLocation localeOrParent(
      @NotNull String locale,
      @Nullable LocaleLocation initial
  ) {
    File file = provider.getLocaleFile(locale);
    InputStream bundled = provider.getLocaleBundle(locale);

    if (file.exists() || bundled != null) {
      return new LocaleLocation(locale, file, bundled);
    }

    if (initial == null) {
      initial = new LocaleLocation(locale, file, null);
    }

    String parent = getParent(locale);

    if (parent == null) {
      return initial;
    }

    return localeOrParent(parent, initial);
  }

  private @Nullable String getParent(@NotNull String locale) {
    int lastSeparator = locale.lastIndexOf('_');

    // Must be at least some content before separator.
    if (lastSeparator < 1) {
      return null;
    }

    return locale.substring(0, lastSeparator);
  }

  private @NotNull YamlConfiguration loadLocale(@NotNull LocaleLocation localeLoc) {
    YamlConfiguration bundled = loadBundled(localeLoc);

    String parentLocale = getParent(localeLoc.locale);
    YamlConfiguration parent = null;
    if (parentLocale != null) {
      // Get or load parent to inherit data from.
      // Note that because the parent will perform the same process to fetch from its parent,
      // this indirectly recursively traverses up the inheritance tree.
      parent = getLocale(parentLocale, false);
    }

    if (!localeLoc.file.exists()) {
      // If the file does not exist on disk, save bundled defaults.
      saveConfig(bundled, localeLoc.file);

      // If the parent locale exists, inherit values from it.
      inheritValues(bundled, parent);

      return bundled;
    }

    // If the file does exist on disk, load it.
    YamlConfiguration localeConfig = YamlConfiguration.loadConfiguration(localeLoc.file);

    // If the file is updated by adding missing keys from bundled locale and defaults, save.
    if (addMissingKeys(localeLoc.locale, localeConfig, parent, bundled)) {
      saveConfig(localeConfig, localeLoc.file);
    }

    // If the parent locale exists, inherit values from it.
    inheritValues(localeConfig, parent);

    return localeConfig;
  }

  private @NotNull YamlConfiguration loadBundled(@NotNull LocaleLocation lang) {
    if (lang.bundled == null) {
      return new YamlConfiguration();
    } else {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(lang.bundled, StandardCharsets.UTF_8))) {
        return YamlConfiguration.loadConfiguration(reader);
      } catch (IOException e) {
        provider.getLogger().log(Level.WARNING, e,
            () -> "[LanguageManager] Unable to load resource " + lang.file.getName());
        return new YamlConfiguration();
      }
    }
  }

  private void inheritValues(@NotNull Configuration config, @Nullable Configuration inherited) {
    if (inherited == null) {
      return;
    }
    inheritValues(config, inherited, config::set);
    Configuration inheritedDefaults = inherited.getDefaults();
    if (inheritedDefaults != null) {
      inheritValues(config, inheritedDefaults, config::addDefault);
    }
  }

  private void inheritValues(
      @NotNull Configuration values,
      @NotNull Configuration inherited,
      @NotNull BiConsumer<String, Object> consumer) {
    for (Entry<String, Object> entry : inherited.getValues(true).entrySet()) {
      // If this is a subsection or already provided, skip.
      if (entry.getValue() instanceof ConfigurationSection
          || values.isSet(entry.getKey())) {
        continue;
      }
      // Otherwise, add the value.
      consumer.accept(entry.getKey(), entry.getValue());
    }
  }

  private void saveConfig(@NotNull YamlConfiguration config, @NotNull File location) {
    try {
      config.save(location);
    } catch (IOException e) {
      provider.getLogger().log(Level.WARNING, e,
          () -> "[LanguageManager] Unable to save resource " + location.getName());
    }
  }

  private boolean addMissingKeys(
      @NotNull String locale,
      @NotNull YamlConfiguration localeConfig,
      @Nullable Configuration parent,
      @NotNull Configuration localeDefaults
  ) {
    List<String> added = new ArrayList<>();
    List<Message> missing = new ArrayList<>();
    // Populate lists with new and missing keys.
    populateMissing(locale, localeConfig, localeDefaults, added, missing);

    if (parent != null) {
      ConfigurationSection parentGuess = parent.getConfigurationSection(GUESS_PATH);
      // Remove any missing inherited keys.
      // Note that since the parent will have defaults set for convenience,
      // it is more accurate to check if the parent has a guess section for the key
      // to see if it is really missing.
      missing.removeIf(next -> parentGuess == null || !parentGuess.isSet(next.key()));
    }

    // If no keys were added, save only if guess section is updated.
    if (added.isEmpty()) {
      return areMissingKeysUpdated(localeConfig, missing);
    }

    for (String newKey : added) {
      // Set all missing keys to defaults.
      localeConfig.set(newKey, localeDefaults.get(newKey));
    }

    provider.getLogger().info(
        () -> "[LanguageManager] Added new translation keys for " + locale + ": "
            + String.join(", ", added));

    return true;
  }

  private void populateMissing(
      @NotNull String locale,
      @NotNull YamlConfiguration localeConfig,
      @NotNull Configuration localeDefaults,
      @NotNull List<String> added,
      @NotNull List<Message> missing
  ) {
    boolean isDefaultLocale = provider.getDefaultLocale().equals(locale);
    for (Message message : provider.getMessages()) {
      // If the key is set already, message is present.
      if (localeConfig.isString(message.key())) {
        continue;
      }
      // Check if the bundled file contains a specific translation.
      String value = localeDefaults.getString(message.key());
      if (value != null) {
        // If the bundle does contain the missing translation, add it.
        added.add(message.key());
        localeConfig.set(message.key(), value);
      } else if (isDefaultLocale) {
        // If this is the default locale, the message defaults are effectively bundled defaults.
        added.add(message.key());
        localeConfig.set(message.key(), message.defaultValue());
      } else {
        // Otherwise, mark the translation as missing.
        missing.add(message);
      }
    }
  }

  private boolean areMissingKeysUpdated(
      @NotNull Configuration localeConfig,
      @NotNull List<Message> missing
  ) {
    boolean updated = false;

    if (missing.isEmpty()) {
      if (localeConfig.isConfigurationSection(GUESS_PATH)) {
        localeConfig.set(GUESS_PATH, null);
        updated = true;
      }
    } else {
      ConfigurationSection guess = localeConfig.getConfigurationSection(GUESS_PATH);
      if (guess == null) {
        guess = localeConfig.createSection(GUESS_PATH);
      }
      for (Message message : missing) {
        if (!guess.isSet(message.key())) {
          guess.set(message.key(), message.defaultValue());
          updated = true;
        }
      }
    }
    return updated;
  }

  private record LocaleLocation(
      @NotNull String locale,
      @NotNull File file,
      @Nullable InputStream bundled) {

  }

}
