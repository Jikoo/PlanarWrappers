package com.github.jikoo.planarwrappers.service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
 * public class VaultPermission extends ProvidedService&lt;net.milkbowl.vault.permission.Permission&gt; {
 *
 *   public VaultPermission(&#64;NotNull Plugin plugin) {
 *     super(plugin);
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
  private boolean setupDone = false;
  private @Nullable Wrapper<T> wrapper = null;

  protected ProvidedService(@NotNull Plugin plugin) {
    this.plugin = plugin;
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

  @SuppressWarnings("unchecked")
  private @Nullable Class<T> getServiceClass() {
    try {
      // Find class with ProvidedService superclass.
      Class<?> clazz = this.getClass();
      for (; clazz != null; clazz = clazz.getSuperclass()) {
        Class<?> superClass = clazz.getSuperclass();
        if (superClass.equals(ProvidedService.class)) {
          break;
        }
      }

      // Should never be possible.
      if (clazz == null) {
        //noinspection ConstantConditions IDE does not understand looping over superclass.
        throw new IllegalStateException(String.format("%s does not subclass ProvidedService!", getClass()));
      }

      // Get ProvidedService Type from class.
      Type type = ProvidedService.this.getClass().getGenericSuperclass();
      // This class is parameterized with a single type. It should always be a ParameterizedType,
      // and there always should be a single type argument that is the class desired.
      if (type instanceof ParameterizedType) {
        Type rawType = ((ParameterizedType) type).getActualTypeArguments()[0];
         return (Class<T>) rawType;
      }

      throw new IllegalStateException("Type " + type.getTypeName() + " is not a ParameterizedType!");
    } catch (TypeNotPresentException ignored) {
      // Class not present.
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
    Class<T> clazz = getServiceClass();
    if (clazz == null) {
      finishSetup(null, logServiceClassNotLoaded());
      return;
    }

    RegisteredServiceProvider<?> registration =
        plugin.getServer().getServicesManager().getRegistration(clazz);

    // Ensure an instance is available.
    if (registration == null) {
      finishSetup(null, logNoProviderRegistered(clazz));
      return;
    }

    T instance = clazz.cast(registration.getProvider());

    // If instance hasn't changed, do nothing.
    if (wrapper != null && wrapper.unwrap().equals(instance)) {
      return;
    }

    // Set setupDone false to log changing instance.
    setupDone = false;

    finishSetup(instance, logServiceProviderChange(clazz, instance));
  }

  protected @Nullable Supplier<@NotNull String> logServiceClassNotLoaded() {
    return () -> "Service is not loaded, cannot use integration";
  }

  protected @Nullable Supplier<@NotNull String> logNoProviderRegistered(@NotNull Class<T> clazz) {
    return () -> "No provider registered with Bukkit for " + clazz.getName();
  }

  protected @Nullable Supplier<String> logServiceProviderChange(
      @NotNull Class<T> clazz,
      @NotNull T instance) {
    return () -> "Hooked into " + clazz.getName() + " provider " + instance.getClass().getName();
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
