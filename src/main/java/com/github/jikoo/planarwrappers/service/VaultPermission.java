package com.github.jikoo.planarwrappers.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A bridge for Vault-supporting permissions plugins. */
public class VaultPermission extends ManagerProvidedService<net.milkbowl.vault.permission.Permission> {

  private final PermissibleBase opPerms;
  private final PermissibleBase defaultPerms;

  public VaultPermission(@NotNull Plugin plugin) {
    super(plugin);
    // Bukkit's SimpleServiceManager will manage keeping these permissibles in sync.
    this.opPerms = new PermissibleBase(new FrozenServerOp(true));
    this.defaultPerms = new PermissibleBase(null);
  }

  @Override
  protected boolean isUsable(@NotNull net.milkbowl.vault.permission.Permission provider) {
    return provider.isEnabled();
  }

  /**
   * Check if a player is granted a permission.
   *
   * @param player the OfflinePlayer to check permissions for
   * @param permission the permission to check
   * @param worldName the optional world name
   * @return true if the player has the permission
   */
  public boolean hasPermission(
      @NotNull OfflinePlayer player,
      @NotNull String permission,
      @Nullable String worldName) {
    Player online = player.getPlayer();
    if (online != null) {
      return online.hasPermission(permission);
    }

    Wrapper<net.milkbowl.vault.permission.Permission> wrapper = getService();

    if (wrapper == null) {
      return getDefaultState(player, permission);
    }

    return wrapper.unwrap().playerHas(worldName, player, permission);
  }

  private boolean getDefaultState(@NotNull OfflinePlayer player, @NotNull String permission) {
    if (player.isOp()) {
      return opPerms.hasPermission(permission);
    }

    return defaultPerms.hasPermission(permission);
  }

  record FrozenServerOp(boolean isOp) implements ServerOperator {
    @Override
      public void setOp(boolean value) {
        throw new UnsupportedOperationException("FrozenServerOp cannot change operator state");
      }
    }

}
