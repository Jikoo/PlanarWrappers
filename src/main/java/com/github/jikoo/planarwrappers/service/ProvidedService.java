package com.github.jikoo.planarwrappers.service;

import java.util.function.Supplier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for an implementation provided by a {@link Plugin} using the
 * {@link org.bukkit.plugin.ServicesManager ServicesManager}.
 *
 * <p>For optimal usage, write your ProvidedService in a way that never exposes the internal class.
 * Instead, create methods that report results of queries you wish to perform.
 * <br>Ex:
 * <pre>
 * /** A bridge for Vault-supporting permissions plugins. &#42;/
 * public class VaultPermission extends ProvidedService&lt;Permission&gt; {
 *
 *   public VaultPermission(&#64;NotNull Plugin plugin) {
 *     super(plugin, "net.milkbowl.vault.permission.Permission");
 *   }
 *
 *   /**
 *    * Check if a player is granted a permission.
 *    *
 *    * &#64;param player the OfflinePlayer to check permissions for
 *    * &#64;param permission the permission to check
 *    * &#64;return true if the player has the permission
 *    &#42;/
 *   public boolean hasPermission(
 *       &#64;NotNull OfflinePlayer player,
 *       &#64;NotNull String permission) {
 *     if (!isPresent()) {
 *       return false;
 *     }
 *
 *     Wrapper&lt;Permission&gt; wrapper = getService();
 *
 *     if (wrapper == null) {
 *       return false;
 *     }
 *
 *     return wrapper.unwrap().playerHas(null, player, permission);
 *   }
 *
 * }
 * </pre>
 *
 * @param <T> the type of service
 */
public abstract class ProvidedService<T> implements Listener {

  private final @NotNull Plugin plugin;
  private final @NotNull String serviceClassName;
  private boolean setupDone = false;
  private @Nullable Wrapper<T> wrapper = null;

  protected ProvidedService(@NotNull Plugin plugin, @NotNull String serviceClassName) {
    this.plugin = plugin;
    this.serviceClassName = serviceClassName;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  public boolean isPresent() {
    return getService() != null;
  }

  protected @Nullable Wrapper<T> getService() {
    // Attempt to initialize if not already set up.
    wrapClass(false);

    return wrapper;
  }

  private @Nullable Class<?> getServiceClass() {
    try {
      return Class.forName(serviceClassName);
    } catch (ClassNotFoundException ignored) {
      return null;
    }
  }

  private boolean isServiceImpl(Class<?> clazz) {
    Class<?> serviceClass = getServiceClass();
    if (serviceClass == null) {
      return false;
    }

    return serviceClass.isAssignableFrom(clazz);
  }

  /**
   * EventHandler for ServiceRegisterEvents in case of a provider being enabled.
   *
   * @param event the ServiceRegisterEvent
   */
  @EventHandler
  public final void onServiceRegister(@NotNull ServiceRegisterEvent event) {
    if (isServiceImpl(event.getProvider().getService())) {
      // If already set up, check if new plugin is a provider overriding existing.
      wrapClass(true);
    }
  }

  /**
   * EventHandler for ServiceUnregisterEvents in case of a provider being disabled.
   *
   * @param event the ServiceUnregisterEvent
   */
  @EventHandler
  public final void onServiceUnregister(@NotNull ServiceUnregisterEvent event) {
    if (isServiceImpl(event.getProvider().getService())) {
      // If already set up, check if plugin was the active provider.
      wrapClass(true);
    }
  }

  /**
   * Attempt to wrap provider. If the setup state does not match the provided value this does
   * nothing to prevent unnecessary loads.
   *
   * @param setupState the expected setup state
   */
  private void wrapClass(boolean setupState) {
    // If no change is likely, have we already obtained the service?
    if (setupState != setupDone) {
      return;
    }

    // Ensure service class is loaded.
    Class<?> clazz = getServiceClass();
    if (clazz == null) {
      finishSetup(null, () -> serviceClassName + " is not loaded, cannot use integration");
      return;
    }

    RegisteredServiceProvider<?> registration =
        plugin.getServer().getServicesManager().getRegistration(clazz);

    // Ensure an instance is available.
    if (registration == null) {
      finishSetup(null, () -> "No provider registered with Bukkit for " + serviceClassName);
      return;
    }

    // Can't be helped - we can't pass an actual class or Bukkit won't load
    // the listeners, and due to the way generics work there's no way to verify
    // that the provided class name is actually the same as the generic class.
    @SuppressWarnings("unchecked")
    T instance = (T) registration.getProvider();

    // If instance hasn't changed, do nothing.
    if (wrapper != null && wrapper.unwrap().equals(instance)) {
      return;
    }

    // Set setupDone false to log changing instance.
    setupDone = false;

    finishSetup(
        instance,
        () -> "Hooked into " + serviceClassName + " provider " + instance.getClass().getName());
  }

  /**
   * Do logging and set state for setup completion.
   *
   * @param value the new service to wrap
   * @param log a supplier for a logging line or null if no logging is to be done
   */
  private void finishSetup(@Nullable T value, @Nullable Supplier<String> log) {
    if (value == null) {
      wrapper = null;
    } else {
      wrapper = new Wrapper<>(value);
    }

    if (log != null && !setupDone) {
      plugin.getLogger().info(log);
    }

    setupDone = true;
  }

  /**
   * Wrapper class used to prevent Bukkit from logging an error and preventing registering events
   * for the listener when service class is not loaded.
   */
  public static class Wrapper<T> {

    private final @NotNull T wrappedObj;

    Wrapper(@NotNull T wrappedObj) {
      this.wrappedObj = wrappedObj;
    }

    public T unwrap() {
      return this.wrappedObj;
    }
  }

}
