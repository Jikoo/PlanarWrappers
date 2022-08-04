package com.github.jikoo.planarwrappers.world;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Define cardinal directions for use in shapes")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DirectionTest {

  @ParameterizedTest
  @CsvSource({
      "-45, SOUTH", "0, SOUTH", "44.9, SOUTH", "720, SOUTH",
      "45, WEST", "90, WEST", "134.9, WEST", "430, WEST",
      "135, NORTH", "180, NORTH", "224.9, NORTH", "-135.1, NORTH",
      "-135, EAST", "-90, EAST", "-45.1, EAST", "270, EAST"})
  void testPlayerFacing(float yaw, Direction direction) {
    Player mock = mock(Player.class);
    when(mock.getLocation()).thenReturn(new Location(null, 0, 0, 0, yaw, 0));
    assertThat("Direction must be obtained from yaw", Direction.getFacingDirection(mock), is(direction));
  }

  @ParameterizedTest
  @MethodSource("getDirectionNames")
  void testSafeValues(String value, Direction expected) {
    assertThat("Direction must be safely parsed", Direction.safeValue(value), is(expected));
  }

  private Stream<Arguments> getDirectionNames() {
    return Arrays.stream(new Arguments[]{
        Arguments.of("invalid direction", Direction.NORTH),
        Arguments.of(null, Direction.NORTH),
        Arguments.of("North", Direction.NORTH),
        Arguments.of("NORTH", Direction.NORTH),
        Arguments.of("South", Direction.SOUTH),
        Arguments.of("SOUTH", Direction.SOUTH),
        Arguments.of("East", Direction.EAST),
        Arguments.of("EAST", Direction.EAST),
        Arguments.of("West", Direction.WEST),
        Arguments.of("WEST", Direction.WEST)
    });
  }
}
