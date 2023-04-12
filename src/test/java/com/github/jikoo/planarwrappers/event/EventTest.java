package com.github.jikoo.planarwrappers.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.jikoo.planarwrappers.mock.BukkitServer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.ServerLoadEvent.LoadType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Feature: Register events using consumers")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventTest {

  Plugin plugin;

  @BeforeAll
  void beforeAll() {
    plugin = mock(Plugin.class);
    doReturn(true).when(plugin).isEnabled();
  }

  @Test
  void testBadEvent() {
    Consumer<ServerEvent> serverEventConsumer = event -> plugin.getLogger().info("Event!");
    assertThrows(
        IllegalArgumentException.class,
        () -> Event.register(ServerEvent.class, serverEventConsumer, plugin));
  }

  @Test
  void testEvent() {
    AtomicBoolean hit = new AtomicBoolean();
    Event.register(ServerCommandEvent.class, event -> hit.set(true), plugin);
    assertThat(
        "Consumer must be registered",
        ServerCommandEvent.getHandlerList().getRegisteredListeners().length,
        greaterThan(0));

    Server server = BukkitServer.newServer();
    CommandSender sender = mock(CommandSender.class);
    ServerCommandEvent event = new ServerCommandEvent(sender, "example");
    PluginManager manager = new SimplePluginManager(server, new SimpleCommandMap(server));
    manager.callEvent(event);

    assertThat("Registered consumer must be called", hit.get());
  }

  @Test
  void testKeyed() {
    String key = "sample text";
    Class<ServerLoadEvent> clazz = ServerLoadEvent.class;
    AtomicInteger value = new AtomicInteger(-1);
    for (int i = 0; i < 2; ++i) {
      final int index = i;
      Event.register(clazz, event -> value.set(index), plugin, key);
    }

    assertThat(
        "Consumer must be registered only once",
        ServerCommandEvent.getHandlerList().getRegisteredListeners().length,
        is(1));

    Server server = BukkitServer.newServer();
    PluginManager manager = new SimplePluginManager(server, new SimpleCommandMap(server));
    manager.callEvent(new ServerLoadEvent(LoadType.RELOAD));

    assertThat("First consumer must not be overridden", value.get(), is(0));

    Event.unregister(clazz, key);

    assertThat(
        "Consumer must be removed by key",
        ServerCommandEvent.getHandlerList().getRegisteredListeners().length,
        is(1));

    // Unregistering nonexistent key should not error.
    assertDoesNotThrow(() -> Event.unregister(clazz, key));
  }
}
