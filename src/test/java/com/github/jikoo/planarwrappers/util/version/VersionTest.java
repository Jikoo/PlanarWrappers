package com.github.jikoo.planarwrappers.util.version;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
class VersionTest {

  @ParameterizedTest
  @MethodSource("getArguments")
  void compare(@NotNull Version version1, Comparison comparison, Version version2) {
    assertThat(
        "Versions must have expected relation",
        version1.lessThan(version2),
        is(comparison == Comparison.LESSER));
    assertThat(
        "Versions must have expected relation",
        version1.lessThanOrEqual(version2),
        is(comparison == Comparison.LESSER || comparison == Comparison.EQUAL));
    assertThat(
        "Versions must have expected relation",
        version1.equals(version2),
        is(comparison == Comparison.EQUAL));
    assertThat(
        "Versions must have expected relation",
        version1.greaterThanOrEqual(version2),
        is(comparison == Comparison.GREATER || comparison == Comparison.EQUAL));
    assertThat(
        "Versions must have expected relation",
        version1.greaterThan(version2),
        is(comparison == Comparison.GREATER));
  }

  static @NotNull Collection<Arguments> getArguments() {
    return addInverse(Arrays.asList(
        Arguments.of(Version.of(1, 2, 3), Comparison.EQUAL, Version.of("1.2.3")),
        Arguments.of(Version.of(1, 2, 3), Comparison.EQUAL, Version.of(1, 2, 3)),
        Arguments.of(Version.of(2, 0, 0), Comparison.EQUAL, Version.of(2)),
        Arguments.of(Version.of("1.2.3"), Comparison.EQUAL, Version.of("1.2.3")),
        Arguments.of(Version.of("1.2.3"), Comparison.GREATER, Version.of("1.2.3-SNAPSHOT")),
        Arguments.of(Version.of("1.2.3-SNAPSHOT"), Comparison.LESSER, Version.of("1.2.3-SNAPSHOT-SNAPSHOT")),
        Arguments.of(Version.of("1.2.3-"), Comparison.LESSER, Version.of("1.2.3-0")),
        Arguments.of(Version.of("1.2.3"), Comparison.GREATER, Version.of("0.1.2.3")),
        Arguments.of(Version.of(1, 2, 3), Comparison.GREATER, Version.of(1, 2, 2)),
        Arguments.of(Version.of("COOL.BEANS.1"), Comparison.LESSER, Version.of("COOL.BEANS.2")),
        Arguments.of(Version.of("COOL.1.BEANS.1"), Comparison.LESSER, Version.of("COOL.2.BEANS.1")),
        Arguments.of(Version.of("v1.2.3-username-alpha-3"), Comparison.LESSER, Version.of("v1.2.3-username-alpha-5")),
        Arguments.of(Version.of("v1.2.3-username-alpha-3"), Comparison.LESSER, Version.of("v1.2.3-username-release-1")),
        Arguments.of(Version.of(1, 0, 0), Comparison.LESSER, Version.of(2)),
        Arguments.of(Version.of(), Comparison.EQUAL, Version.of(0)),
        Arguments.of(Version.of(), Comparison.EQUAL, Version.of("")),
        Arguments.of(Version.of(""), Comparison.LESSER, Version.of("0"))
    ));
  }

  private static @NotNull Collection<Arguments> addInverse(@NotNull Collection<Arguments> arguments) {
    Collection<Arguments> aggregate = new ArrayList<>(arguments);
    for (Arguments argument : arguments) {
      Object[] args = argument.get();
      aggregate.add(Arguments.of(args[2], ((Comparison) args[1]).inverse(), args[0]));
    }
    return aggregate;
  }

  private enum Comparison {
    LESSER, EQUAL, GREATER;

    Comparison inverse() {
      return switch (this) {
        case LESSER -> GREATER;
        case GREATER -> LESSER;
        default -> EQUAL;
      };
    }
  }

}