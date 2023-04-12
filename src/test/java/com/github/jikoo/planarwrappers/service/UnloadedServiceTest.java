package com.github.jikoo.planarwrappers.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@DisplayName("Feature: Gracefully handle classes not loaded at runtime")
@TestInstance(Lifecycle.PER_METHOD)
class UnloadedServiceTest {

  @DisplayName("Hook with unknown/unloaded service must report itself absent")
  @Test
  void testUnloadedService() {
    Plugin plugin = mock(Plugin.class);
    Logger logger = mock(Logger.class);
    doReturn(logger).when(plugin).getLogger();
    TestProvidedService<UnloadedService> service = new TestProvidedService<>(plugin) {};
    assertThat("Unknown service is not present", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));

    service.setValue(null);
    assertThat("Unknown service is not present after other registration", service.isPresent(), is(false));
    assertThat("Wrapper is null", service.getService(), is(nullValue()));
  }

}
