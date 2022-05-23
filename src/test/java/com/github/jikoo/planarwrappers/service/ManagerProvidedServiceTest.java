package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
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

  @BeforeEach
  void beforeEach() {
    MockBukkit.mock();
    plugin = MockBukkit.createMockPlugin("ManagerServiceConsumer");
    registrant = MockBukkit.createMockPlugin("Registrant");
  }

  @AfterEach
  void afterEach() {
    MockBukkit.unmock();
  }

  @DisplayName("Hook must report itself present if a registration was added prior to creation")
  @Test
  void testEarlyRegistration() {
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ManagerProvidedService<Object>(plugin) {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must still be usable after redundant registrations")
  @Test
  void testReregistration() {
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ManagerProvidedService<Object>(plugin) {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support registration after creation")
  @Test
  void testLateRegistration() {
    ProvidedService<Object> service = new ManagerProvidedService<Object>(plugin) {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support deregistration")
  @Test
  void testRemoveRegistration() {
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ManagerProvidedService<Object>(plugin) {};
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().unregisterAll(registrant);
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

}
