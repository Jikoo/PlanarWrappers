package com.github.jikoo.planarwrappers.service;

import static org.mockito.Mockito.mock;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import java.util.Set;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link PluginManagerMock} that passes permission handling to an instance of Bukkit's
 * {@link SimplePluginManager}.
 */
public class PermPassingPluginManager extends PluginManagerMock {

  private final SimplePluginManager simpleManager;

  public PermPassingPluginManager(@NotNull ServerMock server) {
    super(server);
    this.simpleManager = new SimplePluginManager(server, mock(SimpleCommandMap.class));
  }

  @Override
  public Permission getPermission(@NotNull String name) {
    return simpleManager.getPermission(name);
  }

  @Override
  public void addPermission(@NotNull Permission perm) {
    simpleManager.addPermission(perm);
  }

  @Override
  public @NotNull Set<Permission> getDefaultPermissions(boolean op) {
    return simpleManager.getDefaultPermissions(op);
  }

  @Override
  public void removePermission(@NotNull Permission perm) {
    simpleManager.removePermission(perm);
  }

  @Override
  public void removePermission(@NotNull String name) {
    simpleManager.removePermission(name);
  }

  @Override
  public void recalculatePermissionDefaults(@NotNull Permission perm) {
    simpleManager.recalculatePermissionDefaults(perm);
  }

  @Override
  public void subscribeToPermission(@NotNull String permission, @NotNull Permissible permissible) {
    simpleManager.subscribeToPermission(permission, permissible);
  }

  @Override
  public void unsubscribeFromPermission(@NotNull String permission,
      @NotNull Permissible permissible) {
    simpleManager.unsubscribeFromPermission(permission, permissible);
  }

  @Override
  public @NotNull Set<Permissible> getPermissionSubscriptions(@NotNull String permission) {
    return simpleManager.getPermissionSubscriptions(permission);
  }

  @Override
  public @NotNull Set<Permissible> getDefaultPermSubscriptions(boolean op) {
    return simpleManager.getDefaultPermSubscriptions(op);
  }

  @Override
  public @NotNull Set<Permission> getPermissions() {
    return simpleManager.getPermissions();
  }

  @Override
  public void unsubscribeFromDefaultPerms(boolean op, @NotNull Permissible permissible) {
    simpleManager.unsubscribeFromDefaultPerms(op, permissible);
  }

  @Override
  public void subscribeToDefaultPerms(boolean op, @NotNull Permissible permissible) {
    simpleManager.subscribeToDefaultPerms(op, permissible);
  }

}
