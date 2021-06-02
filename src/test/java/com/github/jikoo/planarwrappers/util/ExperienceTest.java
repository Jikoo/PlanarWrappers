package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Manipulate player total experience")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperienceTest {

  private Player player;

  @BeforeAll
  void beforeAll() {
    ServerMock mock = MockBukkit.mock();
    player = mock.addPlayer();
  }

  @DisplayName("Ensure experience modification results in correct values")
  @ParameterizedTest
  @MethodSource("getChanges")
  void testChangeExp(int start, int change) {
    player.setLevel(0);
    player.setExp(0);
    player.setTotalExperience(0);
    player.giveExp(start);

    assertThat("Starting experience must be correct", player.getTotalExperience(), is(start));
    assertThat("Experience calculation must match given", Experience.getExp(player), is(start));

    Experience.changeExp(player, change);

    int finish = Math.max(0, start + change);

    assertThat("Final experience calculation must match", Experience.getExp(player), is(finish));

    if (-start > change) {
      // Full change would set player exp below 0. Reversing sign won't equal start.
      return;
    }

    Experience.changeExp(player, -change);

    assertThat("Reverted experience calculation must match", Experience.getExp(player), is(start));

  }

  private Stream<Arguments> getChanges() {
    // Matrix [0 to 10][-10 to 2000]
    return IntStream.rangeClosed(0, 10).mapToObj(startVal ->
            IntStream.rangeClosed(-10, 2000).mapToObj(changeVal ->
                Arguments.of(startVal, changeVal)))
        .flatMap(Function.identity());
  }

  @AfterAll
  void afterAll() {
    MockBukkit.unmock();
  }

}
