package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.ServerMocks;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Generic hook via the Bukkit ServicesManager")
@TestInstance(Lifecycle.PER_METHOD)
class ManagerProvidedServiceTest {

  Plugin plugin;
  Plugin registrant;
  ServicesManager manager;

  @BeforeEach
  void beforeEach() {
    manager = new SimpleServicesManager();

    Server server = ServerMocks.newServer();
    when(server.getServicesManager()).thenReturn(manager);
    when(server.getPluginManager()).thenReturn(new SimplePluginManager(server, new SimpleCommandMap(server)));

    plugin = mock(Plugin.class);
    when(plugin.getName()).thenReturn("ManagerServiceConsumer");
    when(plugin.isEnabled()).thenReturn(true);
    when(plugin.getServer()).thenReturn(server);
    Logger logger = mock(Logger.class);
    when(plugin.getLogger()).thenReturn(logger);

    registrant = mock(Plugin.class);
    when(registrant.getName()).thenReturn("Registrant");

  }

  @AfterEach
  void afterEach() {
    ServerMocks.unsetBukkitServer();
  }

  @DisplayName("Hook must not report unusable registration as present")
  @Test
  void testUnusableRegistration() {
    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ManagerProvidedService<>(plugin) {
      @Override
      protected boolean isUsable(@NotNull Object provider) {
        return false;
      }
    };
    assertThat("Service is not present", service.isPresent(), is(false));
  }

  @DisplayName("Hook must report itself present if a registration was added prior to creation")
  @Test
  void testEarlyRegistration() {
    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = getService();
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must still be usable after redundant registrations")
  @Test
  void testReregistration() {
    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = getService();
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support registration after creation")
  @Test
  void testLateRegistration() {
    ProvidedService<Object> service = getService();
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support deregistration")
  @Test
  void testRemoveRegistration() {
    manager.register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = getService();
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    manager.unregisterAll(registrant);
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  private @NotNull ManagerProvidedService<Object> getService() {
    return new ManagerProvidedService<>(plugin) {
      @Override
      protected boolean isUsable(@NotNull Object provider) {
        return true;
      }
    };
  }

}
