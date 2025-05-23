package com.github.jikoo.planarwrappers.lang;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link LocaleProvider} for a {@link Plugin}.
 */
public abstract class PluginLocaleProvider implements LocaleProvider {

  private final @NotNull Plugin plugin;
  private final @NotNull File folder;

  protected PluginLocaleProvider(@NotNull Plugin plugin) {
    this.plugin = plugin;
    this.folder = new File(plugin.getDataFolder(), "locale");

    if (!folder.isDirectory() && !folder.mkdirs()) {
      plugin.getLogger().warning(
          () -> "Unable to create " + folder.getPath() + "! Languages may not be editable.");
    }
  }

  @Override
  public @NotNull String getDefaultLocale() {
    return plugin.getConfig().getString("settings.locale", "en");
  }

  @Override
  public boolean isWarnIfGuessExists() {
    return plugin.getConfig().getBoolean("settings.secret.warn-about-guess-section", true);
  }

  @Override
  public @NotNull File getLocaleFile(@NotNull String locale) {
    return new File(folder, locale + ".yml");
  }

  @Override
  public @Nullable InputStream getLocaleBundle(@NotNull String locale) {
    return plugin.getResource("locale/" + locale + ".yml");
  }

  @Override
  public @NotNull Logger getLogger() {
    return plugin.getLogger();
  }

}
