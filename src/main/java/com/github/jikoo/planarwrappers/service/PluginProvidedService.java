package com.github.jikoo.planarwrappers.service;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ProvidedService} directly depending on another {@link Plugin}.
 *
 * @param <T> the type of plugin
 */
public abstract class PluginProvidedService<T extends JavaPlugin> extends ProvidedService<T> {

  protected PluginProvidedService(@NotNull Plugin plugin) {
    super(plugin);
  }

  @Override
  protected @Nullable T getRegistration(@NotNull Class<T> clazz) {
    try {
      JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(clazz);
      if (providingPlugin.isEnabled() && clazz.isInstance(providingPlugin)) {
        return clazz.cast(providingPlugin);
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      // MockBukkit does not support JavaPlugin#getProvidingPlugin, does not use PluginClassLoader.
      for (Plugin loaded : plugin.getServer().getPluginManager().getPlugins()) {
        if (loaded.isEnabled() && clazz.isInstance(loaded)) {
          return clazz.cast(loaded);
        }
      }
    }
    return null;
  }

  /**
   * {@link EventHandler} for {@link PluginEnableEvent PluginEnableEvents} in case of a provider
   * being enabled.
   *
   * @param event the event
   */
  @EventHandler
  public final void onPluginEnable(@NotNull PluginEnableEvent event) {
    handlePlugin(event);
  }

  /**
   * {@link EventHandler} for {@link PluginDisableEvent PluginDisableEvents} in case of a provider
   * being disabled.
   *
   * @param event the event
   */
  @EventHandler
  public final void onPluginDisable(@NotNull PluginDisableEvent event) {
    handlePlugin(event);
  }

  private void handlePlugin(@NotNull PluginEvent event) {
    // If already set up, ensure that provider is present.
    if (isServiceImpl(event.getPlugin().getClass())) {
      wrapClass(true);
    }
  }

}
