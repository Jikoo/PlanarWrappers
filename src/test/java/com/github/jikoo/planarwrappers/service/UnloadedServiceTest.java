package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Gracefully handle classes not loaded at runtime")
@TestInstance(Lifecycle.PER_METHOD)
class UnloadedServiceTest {

  @BeforeEach
  void beforeEach() {
    MockBukkit.mock();
  }

  @AfterEach
  void afterEach() {
    MockBukkit.unmock();
  }

  @DisplayName("Hook with unknown/unloaded service must report itself absent")
  @Test
  void testUnloadedService() {
    Plugin plugin = MockBukkit.createMockPlugin("ServiceConsumer");
    TestProvidedService<UnloadedService> service = new TestProvidedService<>(plugin) {};
    assertThat("Unknown service is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    service.setValue(null);
    assertThat("Unknown service is not present after other registration", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

}
