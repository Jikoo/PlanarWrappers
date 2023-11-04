package com.github.jikoo.planarwrappers.util.version;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

import com.github.jikoo.planarwrappers.mock.ServerMocks;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(Lifecycle.PER_CLASS)
class BukkitVersionsTest {

  @BeforeAll
  void beforeAll() {
    Server server = ServerMocks.newServer();
    doReturn("1.19.4-R0.1-SNAPSHOT").when(server).getBukkitVersion();
    Bukkit.setServer(server);
  }

  @Test
  void basicParse() {
    assertThat(BukkitVersions.MINECRAFT, is(new IntVersion(1, 19, 4)));
    assertThat(BukkitVersions.CRAFTBUKKIT_PACKAGE, is(BukkitVersions.MINECRAFT));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "org.bukkit.craftbukkit.v1_5_RV",
      "org.bukkit.craftbukkit.1_4_5",
      "invalid string",
      "1.4.5-R1.0"
  })
  void invalidCbPackage(String packageName) {
    assertThat(
        "Invalid package must return Minecraft version",
        BukkitVersions.parseCraftbukkitVersion(packageName),
        is(BukkitVersions.MINECRAFT));
  }

  @ParameterizedTest
  @MethodSource("getMcVersions")
  void parseMcVersion(String version, Version expected) {
    assertThat(
        "Version must be parsed from Bukkit version",
        BukkitVersions.parseMinecraftVersion(version),
        is(expected));
  }

  private static Collection<Arguments> getMcVersions() {
    return List.of(
        Arguments.of("1.4.5-R1.0", new IntVersion(1, 4, 5)),
        Arguments.of("1.19.4-R0.1-SNAPSHOT", new IntVersion(1, 19, 4)),
        Arguments.of("1.20-R0.1-SNAPSHOT", new IntVersion(1, 20, 0)),
        Arguments.of("unknown-format+1.10.3-R01 Cool Server Edition", new StringVersion("unknown")),
        Arguments.of("1.10.3+coolserver-R0.1-SNAPSHOT", new IntVersion(1, 10, 3)),
        Arguments.of("weirdserver+1.10.3-R0.1-SNAPSHOT", new StringVersion("weirdserver")),
        Arguments.of("weirdserver-1.10.3-R0.1-SNAPSHOT", new StringVersion("weirdserver")),
        Arguments.of("weirdserver+1.10.3", new StringVersion("weirdserver")),
        Arguments.of("weirdserver1_10_R3", new StringVersion("weirdserver1_10_R3"))
    );
  }

  @ParameterizedTest
  @MethodSource("getCbVersions")
  void parseCbVersion(String version, Version expected) {
    assertThat(
        "Version must be parsed from Craftbukkit package",
        BukkitVersions.parseCraftbukkitVersion(version),
        is(expected));
  }

  private static Collection<Arguments> getCbVersions() {
    return List.of(
        Arguments.of("org.bukkit.craftbukkit.v1_5_R1.other.package", new IntVersion(1, 5, 1)),
        Arguments.of("org.bukkit.craftbukkit.v1_5_R1", new IntVersion(1, 5, 1)),
        Arguments.of("org.bukkit.craftbukkit.v18_19_R50", new IntVersion(18, 19, 50)),
        Arguments.of("org.bukkit.craftbukkit.v1_20_R2", new IntVersion(1, 20, 2))
    );
  }

}