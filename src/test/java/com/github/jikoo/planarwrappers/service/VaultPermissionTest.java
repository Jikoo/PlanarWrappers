package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
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
  private static final String UNDECLARED_PERMISSION = "permission.undeclared";

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
        "False permission is false for PermissibleBase.",
        permHook.hasPermission(player, FALSE_PERMISSION, null),
        is(false));
    assertThat(
        "True permission is true for PermissibleBase.",
        permHook.hasPermission(player, TRUE_PERMISSION, null));
    assertThat(
        "Undeclared permission is not registered.",
        MockBukkit.getMock().getPluginManager().getPermission(UNDECLARED_PERMISSION),
        is(nullValue()));
    assertThat(
        "Undeclared permission uses op status for PermissibleBase.",
        permHook.hasPermission(player, UNDECLARED_PERMISSION, null),
        is(isOp));
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
    PluginManager pluginManager = MockBukkit.mock(new ServerMock() {
      private final PluginManagerMock pluginManagerMock = new PermPassingPluginManager(this);

      @Override
      public @NotNull PluginManagerMock getPluginManager() {
        return this.pluginManagerMock;
      }
    }).getPluginManager();

    // Set up permissions.
    pluginManager.addPermission(
        new org.bukkit.permissions.Permission(TRUE_PERMISSION, PermissionDefault.TRUE));
    pluginManager.addPermission(
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