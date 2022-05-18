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

@DisplayName("Feature: Generic hook for Bukkit services")
@TestInstance(Lifecycle.PER_METHOD)
class ProvidedServiceTest {

  Plugin plugin;

  @BeforeEach
  void beforeEach() {
    MockBukkit.mock();
    plugin = MockBukkit.createMockPlugin("ServiceConsumer");
  }

  @AfterEach
  void afterEach() {
    MockBukkit.unmock();
  }

  @DisplayName("Hook must report itself absent when no registration is present")
  @Test
  void testNoRegistration() {
    ProvidedService<Object> service = new ProvidedService<Object>(plugin) {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook must report itself present if a registration was added prior to creation")
  @Test
  void testEarlyRegistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin) {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must still be usable after redundant registrations")
  @Test
  void testReregistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin) {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support registration after creation")
  @Test
  void testLateRegistration() {
    ProvidedService<Object> service = new ProvidedService<Object>(plugin) {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support deregistration")
  @Test
  void testRemoveRegistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin) {};
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().unregisterAll(registrant);
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook with unknown/unloaded service must report itself absent")
  @Test
  void testUnknownClass() {
    ProvidedService<UnloadedService> service = new ProvidedService<UnloadedService>(plugin){};
    assertThat("Unknown service is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Unknown service is not present after other registration", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

}