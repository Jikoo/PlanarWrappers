package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.OfflinePlayerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import com.github.jikoo.planarwrappers.service.VaultPermission.FrozenServerOp;
import java.util.function.Supplier;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
class VaultPermissionTest {

  private static final String TRUE_PERMISSION = "permission.true";
  private static final String FALSE_PERMISSION = "permission.false";

  Plugin plugin;
  VaultPermission permHook;

  @BeforeAll
  void beforeAll() {
    setUpServer();
    plugin = MockBukkit.createMockPlugin("VaultDependent");
    permHook = newPermHook();
  }

  @AfterEach
  void afterEach() {
    MockBukkit.getMock().getServicesManager().unregisterAll(plugin);
  }

  @AfterAll
  void tearDown() {
    MockBukkit.unmock();
  }

  @Test
  void hasPermissionOnline() {
    ServerMock server = MockBukkit.getMock();
    PlayerMock player = new PlayerMock(server, "Player") {
      @Override
      public Player getPlayer() {
        return this;
      }
      @Override
      public boolean hasPermission(String name) {
        return true;
      }
    };
    assertThat(
        "Online player is prioritized.",
        permHook.hasPermission(player, FALSE_PERMISSION, null));
  }

  @Test
  void hasPermissionProvider() {
    OfflinePlayer player = new OfflinePlayerMock("Player");
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
        "Bukkit's PermissibleBase is used to calculate defaults.",
        permHook.hasPermission(player, FALSE_PERMISSION, null),
        is(isOp));
    assertThat(
        "Bukkit's PermissibleBase is used to calculate defaults.",
        permHook.hasPermission(player, TRUE_PERMISSION, null));
  }

  private @NotNull OfflinePlayer newOppableOffline(boolean isOp) {
    return new OfflinePlayerMock("Player") {
      @Override
      public boolean isOp() {
        return isOp;
      }
    };
  }

  @Test
  void frozenOpSetOp() {
    FrozenServerOp op = new FrozenServerOp(true);
    assertThrows(UnsupportedOperationException.class, () -> op.setOp(false));
  }

  private void setUpServer() {
    // This is a bit of a mess. Mockbukkit's PluginManagerMock does not calculate effective
    // permissions the same way Bukkit does; Bukkit's SimplePluginManager cannot be used directly
    // without breaking Mockbukkit's plugin loading. The easiest solution is just to pass the
    // permission-related queries to a backing SimplePluginManager instance.

    // Spy a ServerMock and its PluginManagerMock, then initialize MockBukkit with the whole mess.
    ServerMock server = spy(new ServerMock());
    PluginManagerMock mockManager = spy(server.getPluginManager());
    when(server.getPluginManager()).thenReturn(mockManager);
    MockBukkit.mock(server);

    // Pass methods to a backing SimplePluginManager.
    PluginManager simplePluginManager = new SimplePluginManager(server, mock(SimpleCommandMap.class));
    when(mockManager.getDefaultPermissions(anyBoolean()))
        .thenAnswer(invocation ->
            simplePluginManager.getDefaultPermissions(invocation.getArgument(0)));
    doAnswer(invocation -> {
      simplePluginManager.unsubscribeFromDefaultPerms(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(mockManager).unsubscribeFromDefaultPerms(anyBoolean(), any(Permissible.class));
    doAnswer(invocation -> {
      simplePluginManager.subscribeToDefaultPerms(invocation.getArgument(0), invocation.getArgument(1));
      return null;
    }).when(mockManager).subscribeToDefaultPerms(anyBoolean(), any(Permissible.class));

    // Finally, set up permissions.
    simplePluginManager.addPermission(
        new org.bukkit.permissions.Permission(TRUE_PERMISSION, PermissionDefault.TRUE));
    simplePluginManager.addPermission(
        new org.bukkit.permissions.Permission(FALSE_PERMISSION, PermissionDefault.FALSE));
  }

  private @NotNull VaultPermission newPermHook() {
    return new VaultPermission(plugin) {
      // Disable logging during tests to reduce clutter.
      @Override
      protected @Nullable Supplier<@NotNull String> logNoProviderRegistered(
          @NotNull Class<Permission> clazz) {
        return null;
      }

      @Override
      protected @Nullable Supplier<@NotNull String> logServiceClassNotLoaded() {
        return null;
      }

      @Override
      protected @Nullable Supplier<String> logServiceProviderChange(
          @NotNull Class<Permission> clazz,
          @NotNull Permission instance) {
        return null;
      }
    };
  }

}