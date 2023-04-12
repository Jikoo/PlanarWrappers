package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Generic hook for features from other plugins")
@TestInstance(Lifecycle.PER_METHOD)
class ProvidedServiceTest {

  Plugin plugin;

  @BeforeEach
  void beforeEach() {
    plugin = mock(Plugin.class);
    Logger logger = mock(Logger.class);
    doReturn(logger).when(plugin).getLogger();
  }

  @DisplayName("Hook must report itself absent when no registration is present")
  @Test
  void testNoRegistration() {
    ProvidedService<Object> service = new TestProvidedService<>(plugin) {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook must report itself present if a registration was added prior to usage")
  @Test
  void testEarlyRegistration() {
    ProvidedService<Object> service = new TestProvidedService<>(plugin, "Hello world") {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must still be usable after redundant registrations")
  @Test
  void testReregistration() {
    TestProvidedService<Object> service = new TestProvidedService<>(plugin, "Hello world") {};
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    service.setValue("Hello world");
    assertThat("Service is present", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support late registration")
  @Test
  void testLateRegistration() {
    TestProvidedService<Object> service = new TestProvidedService<>(plugin) {};
    assertThat("Service with no registration is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    service.setValue("Hello world");
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));
  }

  @DisplayName("Hook must support deregistration")
  @Test
  void testRemoveRegistration() {
    TestProvidedService<Object> service = new TestProvidedService<>(plugin, "Hello world") {};
    assertThat("Service is present after registration", service.isPresent());
    assertThat("Wrapper is not null", service.getService(), is(notNullValue()));

    service.setValue(null);
    assertThat("Service is not present after unregister", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

  @DisplayName("Hook must reify provider class")
  @Test
  void testReifiedServiceClass() {
    TestProvidedService<String> service = new TestProvidedService<>(plugin) {};
    assertThat("Service class is reified", service.getServiceClass(), is(String.class));
  }

  @DisplayName("Hook must not be generic so provider can be reified")
  @Test
  void testNotGeneric() {
    assertThrows(IllegalStateException.class, () -> new TestProvidedService<>(plugin));
  }

}