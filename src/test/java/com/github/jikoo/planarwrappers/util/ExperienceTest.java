package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Manipulate player total experience")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperienceTest {

  private Player player;
  AtomicInteger level = new AtomicInteger();
  AtomicFloat levelProgress = new AtomicFloat();

  @BeforeAll
  void beforeAll() {
    player = mock(Player.class);

    when(player.getLevel()).thenAnswer(invocation -> level.get());
    doAnswer(invocation -> {
      level.set(invocation.getArgument(0, Integer.class));
      return null;
    }).when(player).setLevel(anyInt());

    when(player.getExp()).thenAnswer(invocation -> levelProgress.get());
    doAnswer(invocation -> {
      levelProgress.set(invocation.getArgument(0, Float.class));
      return null;
    }).when(player).setExp(anyFloat());
  }

  @BeforeEach
  void setUp() {
    level.set(0);
    levelProgress.set(0.0F);
  }

  @DisplayName("Ensure calculated level and experience match vanilla.")
  @ParameterizedTest
  @CsvSource({
      "0,0", "1,7", "2,16", "3,27", "4,40", "5,55",
      "6,72", "7,91", "8,112", "9,135", "10,160",
      "11,187", "12,216", "13,247", "14,280", "15,315",
      "16,352", "17,394", "18,441", "19,493", "20,550",
      "21,612", "22,679", "23,751", "24,828", "25,910",
      "26,997", "27,1089", "28,1186", "29,1288", "30,1395",
      "31,1507", "32,1628", "33,1758", "34,1897", "35,2045",
      "36,2202", "37,2368", "38,2543", "39,2727", "40,2920",
      "41,3122", "42,3333", "43,3553", "44,3782", "45,4020",
      "46,4267", "47,4523", "48,4788", "49,5062", "50,5345",
      "51,5637", "52,5938", "53,6248", "54,6567", "55,6895",
      "56,7232", "57,7578", "58,7933", "59,8297", "60,8670",
      "61,9052", "62,9443", "63,9843", "64,10252", "65,10670",
      "66,11097", "67,11533", "68,11978", "69,12432", "70,12895",
      "71,13367", "72,13848", "73,14338", "74,14837", "75,15345",
      "76,15862", "77,16388", "78,16923", "79,17467", "80,18020",
      "81,18582", "82,19153", "83,19733", "84,20322", "85,20920",
      "86,21527", "87,22143", "88,22768", "89,23402", "90,24045",
      "91,24697", "92,25358", "93,26028", "94,26707", "95,27395",
      "96,28092", "97,28798", "98,29513", "99,30237", "100,30970"
  })
  void testVanillaLevelAndExp(int level, int experience) {
    assertThat(
        "Level calculation must match vanilla",
        Experience.getIntLevelFromExp(experience),
        is(level));
    if (level > 0) {
      assertThat(
          "Experience value must be exact point of levelup",
          Experience.getIntLevelFromExp(experience - 1),
          is(level - 1));
    }
    assertThat(
        "Experience calculation must match vanilla",
        Experience.getExpFromLevel(level),
        is(experience));
  }

  @DisplayName("Ensure experience modification results in correct values")
  @ParameterizedTest
  @MethodSource("getChanges")
  void testChangeExp(int start, int change) {
    Experience.changeExp(player, start);

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
    // Matrix [0 to 10][-10 to 1000]
    return IntStream.rangeClosed(0, 10).mapToObj(startVal ->
            IntStream.rangeClosed(-10, 1000).mapToObj(changeVal ->
                Arguments.of(startVal, changeVal)))
        .flatMap(Function.identity());
  }
}
