package com.github.jikoo.planarwrappers.lang;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

class PluginLocaleProviderTest {


  @Nested
  class BlockedDirectoriesTest {

    @Captor ArgumentCaptor<Supplier<String>> logging;
    private AutoCloseable closeable;

    @BeforeEach
    public void open() {
      closeable = MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    public void release() throws Exception {
      closeable.close();
    }

    @Test
    void makeDirectories() {
      Plugin plugin = mock();
      doReturn(new File("src/test/resources/lang/bad")).when(plugin).getDataFolder();
      Logger logger = mock();
      doReturn(logger).when(plugin).getLogger();

      new PluginLocaleProvider(plugin) {
        @Override
        public @NotNull Iterable<@NotNull Message> getMessages() {
          return List.of();
        }
      };

      verify(plugin).getLogger();
      verify(logger).warning(logging.capture());
      String logged = logging.getValue().get();
      assertThat(
          "Expected line was logged",
          logged,
          both(startsWith("Unable to create")).and(endsWith("Languages may not be editable.")));
    }
  }

  @Test
  void makeDirectories() {
    File dir = new File("src/test/resources/lang/makedirs");
    if (dir.exists() && !dir.delete()) {
      throw new IllegalStateException("Directories not cleaned up before makeDirectories!");
    }

    Plugin plugin = mock();

    doReturn(dir).when(plugin).getDataFolder();
    Logger logger = mock();
    doReturn(logger).when(plugin).getLogger();

    new PluginLocaleProvider(plugin) {
      @Override
      public @NotNull Iterable<@NotNull Message> getMessages() {
        return List.of();
      }
    };

    verify(plugin, times(0)).getLogger();
    assertThat("Directory must be created", dir.isDirectory());
    File subdir = new File(dir, "locale");
    assertThat("Subdirectory must be created", subdir.isDirectory());

    dir.deleteOnExit();
    subdir.deleteOnExit();
  }

  @Test
  void getDefaultLocale() {
    ProviderPair providerPair = newProvider();
    Plugin plugin = providerPair.plugin;
    doReturn(new YamlConfiguration()).when(plugin).getConfig();

    assertThat("Setting defaults to \"en\"", providerPair.provider.getDefaultLocale(), is("en"));
    verify(plugin).getConfig();
  }

  @Test
  void isWarnIfGuessExists() {
    ProviderPair providerPair = newProvider();
    Plugin plugin = providerPair.plugin;
    doReturn(new YamlConfiguration()).when(plugin).getConfig();

    assertThat("Setting defaults to true", providerPair.provider.isWarnIfGuessExists());
    verify(plugin).getConfig();
  }

  @Test
  void getLocaleFile() {
    String locale = "en";
    ProviderPair providerPair = newProvider();

    File localeFile = providerPair.provider.getLocaleFile(locale);
    assertThat("Locale file is provided as expected", localeFile, is(notNullValue()));
    // Could assert that the file matches expected location,
    // but we'd basically just be testing if Java's file system works.
  }

  @Test
  void getLocaleBundle() {
    ProviderPair providerPair = newProvider();
    PluginLocaleProvider provider = providerPair.provider;

    assertThat("Stream is null", provider.getLocaleBundle("en"), is(nullValue()));

    Plugin plugin = providerPair.plugin;
    verify(plugin).getResource(anyString());
  }

  @Test
  void getLogger() {
    ProviderPair providerPair = newProvider();
    PluginLocaleProvider provider = providerPair.provider;

    assertThat("Logger is not null", provider.getLogger(), is(notNullValue()));

    Plugin plugin = providerPair.plugin;
    verify(plugin).getLogger();
  }

  private static ProviderPair newProvider() {
    Plugin plugin = mock();
    doReturn(new File("src/test/resources/lang/good")).when(plugin).getDataFolder();
    Logger logger = mock();
    doReturn(logger).when(plugin).getLogger();

    var provider = new PluginLocaleProvider(plugin) {
      @Override
      public @NotNull Iterable<@NotNull Message> getMessages() {
        return List.of();
      }
    };

    verify(plugin, never()).getLogger();

    return new ProviderPair(plugin, provider);
  }

  private record ProviderPair(Plugin plugin, PluginLocaleProvider provider) {

  }

}
