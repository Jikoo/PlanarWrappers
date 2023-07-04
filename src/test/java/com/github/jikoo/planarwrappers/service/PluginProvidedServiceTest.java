package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.github.jikoo.planarwrappers.mock.ServerMocks;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Generic hook for a specific plugin")
@TestInstance(Lifecycle.PER_METHOD)
class PluginProvidedServiceTest {

  List<Plugin> enabledPlugins;
  Server server;
  Plugin plugin;

  @BeforeEach
  void beforeEach() {
    server = ServerMocks.newServer();

    // Mock scheduler
    BukkitScheduler scheduler = mock(BukkitScheduler.class);
    doAnswer(invocation -> {
      invocation.getArgument(1, Runnable.class).run();
      return null;
    }).when(scheduler).runTask(any(Plugin.class), any(Runnable.class));
    doReturn(scheduler).when(server).getScheduler();

    // Mock services SimplePluginManager depends on.
    ServicesManager services = mock(ServicesManager.class);
    doReturn(services).when(server).getServicesManager();
    Messenger messenger = mock(Messenger.class);
    doReturn(messenger).when(server).getMessenger();
    doReturn(List.of()).when(server).getWorlds();

    // Set up plugin manager.
    PluginManager plugins = spy(new SimplePluginManager(server, new SimpleCommandMap(server)));
    // Set up enabling plugins.
    enabledPlugins = new ArrayList<>();
    doAnswer(invocation -> {
      invocation.callRealMethod();
      Plugin enabling = invocation.getArgument(0);
      enabledPlugins.add(enabling);
      PluginEnableEvent event = new PluginEnableEvent(enabling);
      plugins.callEvent(event);
      return null;
    }).when(plugins).enablePlugin(any(Plugin.class));
    // Set up disabling plugins.
    doAnswer(invocation -> {
      invocation.callRealMethod();
      Plugin disabling = invocation.getArgument(0);
      enabledPlugins.remove(disabling);
      PluginDisableEvent event = new PluginDisableEvent(disabling);
      plugins.callEvent(event);
      return null;
    }).when(plugins).disablePlugin(any(Plugin.class));
    doAnswer(invocation -> enabledPlugins.toArray(new Plugin[0])).when(plugins).getPlugins();
    doAnswer(invocation -> {
      Plugin plugin = invocation.getArgument(0);
      return plugin.isEnabled();
    }).when(plugins).isPluginEnabled(any(Plugin.class));
    doReturn(plugins).when(server).getPluginManager();

    plugin = mock(Plugin.class);
    doReturn(server).when(plugin).getServer();
    doReturn(true).when(plugin).isEnabled();
    Logger logger = mock(Logger.class);
    doReturn(logger).when(plugin).getLogger();
    doReturn("PluginServiceConsumer").when(plugin).getName();
    enabledPlugins.add(plugin);
  }

  @DisplayName("Hook must report itself present if a registration was added prior to creation")
  @Test
  void testEarlyRegistration() {
    addRegistrant();
    ProvidedService<RegistrantPlugin> service = getService();
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must ignore non-registrant in event")
  @Test
  void testNonRegistrantEvent() {
    ProvidedService<RegistrantPlugin> service = getService();
    server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
    assertThat("Service is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook must still be usable after redundant registrations")
  @Test
  void testReregistration() {
    Plugin registrant = addRegistrant();
    ProvidedService<RegistrantPlugin> service = getService();
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    server.getPluginManager().callEvent(new PluginEnableEvent(registrant));
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support registration after creation")
  @Test
  void testLateRegistration() {
    ProvidedService<RegistrantPlugin> service = getService();
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    addRegistrant();
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support deregistration")
  @Test
  void testRemoveRegistration() {
    ProvidedService<RegistrantPlugin> service = getService();
    Plugin registrant = addRegistrant();
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    server.getPluginManager().disablePlugin(registrant);

    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @Contract(" -> new")
  private @NotNull Plugin addRegistrant() {
    Plugin registrant = mock(RegistrantPlugin.class);
    doReturn("Registrant").when(registrant).getName();

    // Enable/disable for SimplePluginManager
    PluginLoader loader = mock(PluginLoader.class);
    doReturn(loader).when(registrant).getPluginLoader();
    AtomicBoolean enabled = new AtomicBoolean(true);
    doAnswer(invocation -> {
      enabled.set(true);
      return null;
    }).when(loader).enablePlugin(registrant);
    doAnswer(invocation -> {
      enabled.set(false);
      return null;
    }).when(loader).disablePlugin(registrant);
    doAnswer(invocation -> enabled.get()).when(registrant).isEnabled();

    server.getPluginManager().enablePlugin(registrant);

    return registrant;
  }

  @Contract(" -> new")
  private @NotNull ProvidedService<RegistrantPlugin> getService() {
    return new PluginProvidedService<>(plugin){};
  }

  private interface RegistrantPlugin extends Plugin {}

}
