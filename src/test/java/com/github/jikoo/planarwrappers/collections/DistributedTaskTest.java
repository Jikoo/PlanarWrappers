package com.github.jikoo.planarwrappers.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.scheduler.DistributedTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;

@TestInstance(Lifecycle.PER_CLASS)
class DistributedTaskTest {

  private static final int TASK_BUCKETS = 5;

  Plugin plugin;
  Runnable runTask;

  @BeforeAll
  void beforeAll() {
    plugin = mock(Plugin.class);

    // Set up scheduler mock to pull DistributedTask#run as a runnable.
    BukkitScheduler scheduler = mock(BukkitScheduler.class);
    when(scheduler.scheduleSyncRepeatingTask(any(), any(Runnable.class), anyLong(), anyLong()))
        .thenAnswer((Answer<Integer>) invocation -> {
          runTask = invocation.getArgument(1);
          return 2;
        });
    doNothing().when(scheduler).cancelTask(anyInt());

    Server server = mock(Server.class);
    doReturn(scheduler).when(server).getScheduler();

    doReturn(server).when(plugin).getServer();
  }

  @AfterEach
  void afterEach() {
    //noinspection ConstantConditions
    runTask = null;
  }

  @Contract("_ -> new")
  private @NotNull DistributedTask<Object> createTask(Consumer<Collection<Object>> consumer) {
    DistributedTask<Object> task = new DistributedTask<>(
        TASK_BUCKETS * 50,
        TimeUnit.MILLISECONDS,
        consumer);
    task.schedule(plugin);
    return task;
  }

  private void tickAllBuckets() {
    for (int i = 0; i < TASK_BUCKETS; ++i) {
      runTask.run();
    }
  }

  @ParameterizedTest
  @ValueSource(ints = { -50, 0, 50 })
  void testCreateUselessTask(int value) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DistributedTask<>(value, TimeUnit.MILLISECONDS, objects -> {}),
        "Distributed task with one bucket is useless.");
  }

  @Test
  void testAddContent() {
    List<Object> objects = Arrays.asList("test", "successful");
    List<Object> handledObjects = new ArrayList<>();

    DistributedTask<Object> task = createTask(handledObjects::addAll);
    objects.forEach(task::add);

    tickAllBuckets();

    assertThat(handledObjects, hasSize(objects.size()));
    assertThat(handledObjects, containsInAnyOrder(objects.toArray()));
  }

  @Test
  void testDuplicateContentIgnored() {
    List<Object> objects = Arrays.asList("test", "successful");
    List<Object> handledObjects = new ArrayList<>();

    DistributedTask<Object> task = createTask(handledObjects::addAll);
    objects.forEach(task::add);
    objects.forEach(task::add);

    tickAllBuckets();

    assertThat(handledObjects, hasSize(objects.size()));
    assertThat(handledObjects, containsInAnyOrder(objects.toArray()));
  }

  @Test
  void testRemoveContent() {
    List<Object> objects = Arrays.asList("test", "successful");
    assertThat(
        "Must test with two or more objects to cover conditionals",
        objects,
        hasSize(greaterThan(1)));
    List<Object> handledObjects = new ArrayList<>();

    DistributedTask<Object> task = createTask(handledObjects::addAll);
    objects.forEach(task::add);
    objects.forEach(task::remove);
    // Double remove to test conditional.
    objects.forEach(task::remove);

    tickAllBuckets();

    assertThat(handledObjects, hasSize(0));

  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2, 3, 10 })
  void testContentDistribution(int elementsPerBucket) {
    DistributedTask<Object> task = createTask(
        objects ->
            assertThat(
                "Objects must be spread evenly across buckets",
                objects,
                hasSize(elementsPerBucket)));

    for (int i = 0; i < elementsPerBucket * TASK_BUCKETS; ++i) {
      task.add(i);
    }

    tickAllBuckets();
  }

  @Test
  void testContentTicks() {
    AtomicInteger timesCalled = new AtomicInteger();

    DistributedTask<Object> task = createTask(
        objects -> {
          assertThat("Each bucket contains one element", objects, hasSize(1));
          Object object = objects.stream().findFirst().orElse(null);
          assertThat("Object must be an AtomicInteger", object, instanceOf(AtomicInteger.class));
          AtomicInteger integer = (AtomicInteger) object;
          assert integer != null; // Compiler doesn't realize instanceOf fails on null
          assertThat(
              "Times called must match expected",
              integer.incrementAndGet(),
              is(timesCalled.get()));
        });

    AtomicInteger[] atomicIntegerArray = new AtomicInteger[TASK_BUCKETS];
    Arrays.setAll(atomicIntegerArray, ignoredIndex -> new AtomicInteger());

    for (AtomicInteger atomicInteger : atomicIntegerArray) {
      task.add(atomicInteger);
    }

    task.schedule(plugin);

    while (timesCalled.incrementAndGet() < 5) {
      tickAllBuckets();
    }
  }

  @Test
  void testUnscheduledCancel() {
    new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {}).cancel(plugin);
  }

  @Test
  void testScheduleCancel() {
    DistributedTask<Object> task = new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {});
    task.schedule(plugin);
    task.cancel(plugin);
  }

  @Test
  void testDuplicateSchedule() {
    DistributedTask<Object> task = new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {});
    task.schedule(plugin);
    task.schedule(plugin);
  }

}