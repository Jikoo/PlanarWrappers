package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.plugin.PluginManagerMock;
import java.io.File;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Generic hook for a specific plugin")
@TestInstance(Lifecycle.PER_METHOD)
class PluginProvidedServiceTest {

  ServerMock server;
  Plugin plugin;

  @BeforeEach
  void beforeEach() {
    server = MockBukkit.mock();
    plugin = MockBukkit.createMockPlugin("PluginServiceConsumer");
  }

  @AfterEach
  void afterEach() {
    server.getPluginManager().clearPlugins();
    MockBukkit.unmock();
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
    plugin.getServer().getPluginManager().callEvent(new PluginEnableEvent(plugin));
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

    plugin.getServer().getPluginManager().callEvent(new PluginEnableEvent(registrant));
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
    server.getScheduler().performOneTick();
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @Contract(" -> new")
  private @NotNull Plugin addRegistrant() {
    PluginManagerMock pluginManager = server.getPluginManager();
    Plugin registrant = pluginManager.loadPlugin(
        RegistrantPlugin.class,
        new PluginDescriptionFile(
            "Registrant",
            "1.0.0",
            "com.github.planarwrappers.service.PluginProvidedServiceTest$RegistrantPlugin"));
    pluginManager.enablePlugin(registrant);
    return registrant;
  }

  @Contract(" -> new")
  private @NotNull ProvidedService<RegistrantPlugin> getService() {
    return new PluginProvidedService<>(plugin){};
  }

  private static class RegistrantPlugin extends JavaPlugin {
    public RegistrantPlugin(@NotNull final JavaPluginLoader loader, @NotNull final PluginDescriptionFile description, @NotNull final File dataFolder, @NotNull final File file) {
      super(loader, description, dataFolder, file);
    }
  }

}
