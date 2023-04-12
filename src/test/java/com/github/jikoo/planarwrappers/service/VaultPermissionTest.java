package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.BukkitServer;
import com.github.jikoo.planarwrappers.service.VaultPermission.FrozenServerOp;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
class VaultPermissionTest {

  private static final String TRUE_PERMISSION = "permission.true";
  private static final String FALSE_PERMISSION = "permission.false";
  private static final String UNDECLARED_PERMISSION = "permission.undeclared";

  Server server;
  Plugin plugin;
  VaultPermission permHook;

  @BeforeEach
  void beforeEach() {
    server = BukkitServer.newServer();
    ServicesManager services = new SimpleServicesManager();
    doReturn(services).when(server).getServicesManager();
    PluginManager plugins = new SimplePluginManager(server, new SimpleCommandMap(server));
    doReturn(plugins).when(server).getPluginManager();
    Bukkit.setServer(server);

    // Set up permissions.
    plugins.addPermission(
        new org.bukkit.permissions.Permission(TRUE_PERMISSION, PermissionDefault.TRUE));
    plugins.addPermission(
        new org.bukkit.permissions.Permission(FALSE_PERMISSION, PermissionDefault.FALSE));

    plugin = mock(Plugin.class);
    doReturn(server).when(plugin).getServer();
    doReturn("VaultDependent").when(plugin).getName();
    Logger logger = mock(Logger.class);
    doReturn(logger).when(plugin).getLogger();

    permHook = new VaultPermission(plugin);
  }

  @AfterEach
  void afterEach() {
    BukkitServer.unsetBukkitServer();
  }

  @Test
  void hasPermissionOnline() {
    Player player = mock(Player.class);
    doReturn(player).when(player).getPlayer();
    doReturn(true).when(player).hasPermission(anyString());
    assertThat(
        "Online player is prioritized.",
        permHook.hasPermission(player, FALSE_PERMISSION, null));
  }

  @Test
  void hasPermissionProvider() {
    OfflinePlayer player = mock(OfflinePlayer.class);
    Permission permission = registerPerm();
    when(permission.playerHas(null, player, FALSE_PERMISSION)).thenReturn(true);
    assertThat(
        "Provider overrides defaults.",
        permHook.hasPermission(player, FALSE_PERMISSION, null));
  }

  private @NotNull Permission registerPerm() {
    Permission perm = mock(Permission.class);
    when(perm.getName()).thenReturn("MockPerms");
    when(perm.isEnabled()).thenReturn(true);
    plugin.getServer().getServicesManager()
        .register(Permission.class, perm, plugin, ServicePriority.Normal);
    return perm;
  }

  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  void getDefaultState(boolean isOp) {
    OfflinePlayer player = newOppableOffline(isOp);
    assertThat(
        "False permission is false for PermissibleBase.",
        permHook.hasPermission(player, FALSE_PERMISSION, null),
        is(false));
    assertThat(
        "True permission is true for PermissibleBase.",
        permHook.hasPermission(player, TRUE_PERMISSION, null));
    assertThat(
        "Undeclared permission is not registered.",
        server.getPluginManager().getPermission(UNDECLARED_PERMISSION),
        is(nullValue()));
    assertThat(
        "Undeclared permission uses op status for PermissibleBase.",
        permHook.hasPermission(player, UNDECLARED_PERMISSION, null),
        is(isOp));
  }

  private @NotNull OfflinePlayer newOppableOffline(boolean isOp) {
    OfflinePlayer player = mock(OfflinePlayer.class);
    doReturn(isOp).when(player).isOp();
    return player;
  }

  @Test
  void frozenOpSetOp() {
    FrozenServerOp op = new FrozenServerOp(true);
    assertThrows(UnsupportedOperationException.class, () -> op.setOp(false));
  }

}