package com.github.jikoo.planarwrappers.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

@TestInstance(Lifecycle.PER_CLASS)
class DistributedTaskTest {

  private static final int TASK_BUCKETS = 5;

  Plugin plugin;
  ArgumentCaptor<Runnable> runnableCaptor;
  ArgumentCaptor<Long> delay;
  ArgumentCaptor<Long> period;

  @BeforeAll
  void beforeAll() {
    plugin = mock(Plugin.class);
  }

  @BeforeEach
  void beforeEach() {
    runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    delay = ArgumentCaptor.forClass(Long.class);
    period = ArgumentCaptor.forClass(Long.class);

    // Set up scheduler mock to pull DistributedTask#run as a runnable.
    BukkitScheduler scheduler = mock(BukkitScheduler.class);
    doAnswer(invocation -> 2)
        .when(scheduler)
        .scheduleSyncRepeatingTask(any(), runnableCaptor.capture(), delay.capture(), period.capture());

    Server server = mock(Server.class);
    doReturn(scheduler).when(server).getScheduler();
    doReturn(server).when(plugin).getServer();
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
    Runnable runnable = runnableCaptor.getValue();
    for (int i = 0; i < TASK_BUCKETS; ++i) {
      runnable.run();
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
    DistributedTask<Object> unscheduledTask = new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {});
    assertThat("No task scheduled", runnableCaptor.getAllValues().isEmpty());
    assertDoesNotThrow(() -> unscheduledTask.cancel(plugin));
  }

  @Test
  void testScheduleCancel() {
    DistributedTask<Object> task = new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {});
    assertThat("No task scheduled", runnableCaptor.getAllValues().isEmpty());
    assertDoesNotThrow(() -> task.schedule(plugin));
    assertThat("One task scheduled", runnableCaptor.getAllValues(), hasSize(1));
    assertDoesNotThrow(() -> task.cancel(plugin));
  }

  @Test
  void testDuplicateSchedule() {
    DistributedTask<Object> task = new DistributedTask<>(100, TimeUnit.MILLISECONDS, ignored -> {});
    BukkitScheduler scheduler = plugin.getServer().getScheduler();

    verify(scheduler, times(0))
        .scheduleSyncRepeatingTask(any(Plugin.class), any(Runnable.class), anyLong(), anyLong());

    assertDoesNotThrow(() -> task.schedule(plugin));

    verify(scheduler)
        .scheduleSyncRepeatingTask(any(Plugin.class), any(Runnable.class), anyLong(), anyLong());
    verify(scheduler, times(0)).cancelTask(anyInt());

    assertDoesNotThrow(() -> task.schedule(plugin));

    verify(scheduler, times(2))
        .scheduleSyncRepeatingTask(any(Plugin.class), any(Runnable.class), anyLong(), anyLong());
    verify(scheduler).cancelTask(anyInt());
  }

}