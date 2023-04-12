package com.github.jikoo.planarwrappers.service;

import com.github.jikoo.planarwrappers.event.Event;
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
public abstract class PluginProvidedService<T extends Plugin> extends ProvidedService<T> {

  protected PluginProvidedService(@NotNull Plugin plugin) {
    super(plugin);

    Event.register(PluginEnableEvent.class, this::handlePlugin, plugin);
    Event.register(
        PluginDisableEvent.class,
        event -> plugin.getServer().getScheduler().runTask(plugin, () -> handlePlugin(event)),
        plugin);
  }

  @Override
  protected @Nullable T getRegistration(@NotNull Class<T> clazz) {
    try {
      JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(clazz);
      if (providingPlugin.isEnabled() && clazz.isInstance(providingPlugin)) {
        return clazz.cast(providingPlugin);
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      // Mocking static methods is annoying.
      // It's safer and easier to have a fallback in case the provider Plugin is not a JavaPlugin.
      for (Plugin loaded : plugin.getServer().getPluginManager().getPlugins()) {
        if (loaded.isEnabled() && clazz.isInstance(loaded)) {
          return clazz.cast(loaded);
        }
      }
    }
    return null;
  }

  private void handlePlugin(@NotNull PluginEvent event) {
    // If already set up, ensure that provider is present.
    if (isServiceImpl(event.getPlugin().getClass())) {
      wrapClass(true);
    }
  }

}
