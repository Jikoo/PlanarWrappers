package com.github.jikoo.planarwrappers.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
class UninstantiableUtilTest {

  private static Collection<? extends Class<?>> utilClasses = null;

  @ParameterizedTest
  @MethodSource("getUtilClasses")
  void isFinal(@NotNull Class<?> clazz) {
    assertThat("Utility class must be final", Modifier.isFinal(clazz.getModifiers()));
  }

  @ParameterizedTest
  @MethodSource("getUtilClasses")
  void instantiatingThrows(@NotNull Class<?> clazz) {
    var declaredConstructors = clazz.getDeclaredConstructors();
    assertThat("Utility class must only have one constructor.", declaredConstructors, is(arrayWithSize(1)));
    var constructor = declaredConstructors[0];
    assertThat("Utility class constructor must not accept parameters.", constructor.getParameterCount(), is(0));
    assertThat("Utility class constructor must be private.", Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    var thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
    assertThat("Construction denial should be consistent.", thrown.getCause(), is(instanceOf(IllegalStateException.class)));
    assertThat("Construction denial should be consistent.", thrown.getCause().getMessage(), is("Cannot instantiate static utility classes!"));
  }

  static Collection<? extends Class<?>> getUtilClasses() {
    if (utilClasses != null) {
      return utilClasses;
    }

    // Walk all files in ./target/classes, which should be only freshly-compiled non-test classes
    // from this project.
    Path basePath = Path.of("target", "classes");
    try (Stream<Path> files = Files.walk(basePath)) {
      utilClasses = files
          // Ensure the file is a regular file with .class filetype.
          .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class"))
          // Relativize paths against the base path to strip off non-package folders.
          .map(basePath::relativize)
          .map(Path::toString)
          .map(path -> {
            // Strip ".class" suffix and replace file separators with periods.
            path = path.substring(0, path.length() - 6).replace(File.separatorChar, '.');
            try {
              return Class.forName(path);
            } catch (ClassNotFoundException exception) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .filter(clazz -> {
            // Enums are, by nature, already about as final as it gets.
            if (clazz.isEnum()) {
              return false;
            }
            Method[] declaredMethods = clazz.getDeclaredMethods();
            if (declaredMethods.length == 0) {
              // If there are no declared methods, probably a more specific inheritor.
              return false;
            }
            int ignored = 0;
            for (Method declaredMethod : declaredMethods) {
              if (declaredMethod.isSynthetic()) {
                // Also ignore synthetic methods.
                ++ignored;
                continue;
              }
              if (!Modifier.isStatic(declaredMethod.getModifiers())) {
                return false;
              }
            }
            return ignored < declaredMethods.length;
          }).toList();
    } catch (IOException e) {
      utilClasses = List.of();
    }

    return utilClasses;
  }

}
