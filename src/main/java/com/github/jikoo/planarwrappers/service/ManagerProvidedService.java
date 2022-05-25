package com.github.jikoo.planarwrappers.service;

import java.util.Collection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServiceEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ProvidedService} using the {@link org.bukkit.plugin.ServicesManager ServicesManager}.
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
 * @see com.github.jikoo.planarwrappers.service.ProvidedService
 * @param <T> the type of service
 */
public abstract class ManagerProvidedService<T> extends ProvidedService<T> {

  protected ManagerProvidedService(@NotNull Plugin plugin) {
    super(plugin);
  }

  @Override
  @Nullable T getRegistration(@NotNull Class<T> clazz) {
    Collection<RegisteredServiceProvider<T>> registrations = plugin.getServer().getServicesManager()
        .getRegistrations(clazz);
    // Registration listings are ordered by priority by the SimpleServiceManager.
    for (RegisteredServiceProvider<T> registration : registrations) {
      T provider = registration.getProvider();

      // Ensure that the provider is usable. Certain providers can be disabled via external means.
      if (isUsable(provider)) {
        return provider;
      }
    }

    return null;
  }

  protected abstract boolean isUsable(@NotNull T provider);

  /**
   * {@link EventHandler} for {@link ServiceRegisterEvent ServiceRegisterEvents} in case of a
   * provider being registered.
   *
   * @param event the event
   */
  @EventHandler
  public final void onServiceRegister(@NotNull ServiceRegisterEvent event) {
    handleService(event);
  }

  /**
   * {@link EventHandler} for {@link ServiceUnregisterEvent ServiceUnregisterEvents} in case of a
   * provider being unregistered.
   *
   * @param event the event
   */
  @EventHandler
  public final void onServiceUnregister(@NotNull ServiceUnregisterEvent event) {
    handleService(event);
  }

  private void handleService(@NotNull ServiceEvent event) {
    // If already set up, ensure that provider is present.
    if (isServiceImpl(event.getProvider().getService())) {
      wrapClass(true);
    }
  }

}
