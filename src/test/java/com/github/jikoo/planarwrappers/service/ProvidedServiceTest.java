package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "java.lang.Object") {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook must report itself present if a registration was added prior to creation")
  @Test
  void testEarlyRegistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "java.lang.Object") {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("")
  @Test
  void testReregistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "java.lang.Object") {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("")
  @Test
  void testLateRegistration() {
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "java.lang.Object") {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("")
  @Test
  void testRemoveRegistration() {
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "java.lang.Object") {};
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    registrant.getServer().getServicesManager().unregisterAll(registrant);
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook with unknown/unloaded service must report itself absent")
  @Test
  void testUnknownClass() {
    ProvidedService<Object> service = new ProvidedService<Object>(plugin, "InvalidClass"){};
    assertThat("Unknown service is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "Hello World", registrant, ServicePriority.Normal);
    assertThat("Unknown service is not present after other registration", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Mismatching generic and class name error only on usage")
  @Test
  void testGenericMismatch() {
    // Unfortunately, due to how generics work there is no way to verify that the generic and class
    // name match prior to usage as an instance of the generic in live code. It's an easy fix when
    // it occurs, but it cannot be caught ahead of time by the ProvidedService class.
    ProvidedService<ServicePriority> service = new ProvidedService<ServicePriority>(plugin, "java.lang.Object") {};
    Plugin registrant = MockBukkit.createMockPlugin("Registrant");
    registrant.getServer().getServicesManager().register(Object.class, "not a ServicePriority", registrant, ServicePriority.Normal);
    assertThat("Generic mismatching expected class is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
    assertThrows(
        ClassCastException.class,
        () -> {
          @SuppressWarnings("unused") // Used to cause ClassCastException
          ServicePriority priority = service.getService().unwrap();
        },
        "Generic mismatch throws ClassCastException");
  }

}