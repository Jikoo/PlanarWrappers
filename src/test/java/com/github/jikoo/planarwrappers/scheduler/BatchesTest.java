package com.github.jikoo.planarwrappers.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.function.TriFunction;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

@DisplayName("Batch BukkitScheduler operations")
@TestInstance(Lifecycle.PER_CLASS)
class BatchesTest {

  abstract static class BatchTest {

    private final TriFunction<Plugin, Long, TimeUnit, Batch<String>> ctor;

    BatchTest(TriFunction<Plugin, Long, TimeUnit, Batch<String>> ctor) {
      this.ctor = ctor;
    }

    @Test
    void testGatherPeriod() {
      Plugin plugin = mock(Plugin.class);
      assertDoesNotThrow(() -> ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
    }

    @ParameterizedTest
    @ValueSource(longs = { -50, 0, 49 })
    void testBadGatherPeriod(long delay) {
      Plugin plugin = mock(Plugin.class);
      assertThrows(IllegalArgumentException.class, () -> ctor.apply(plugin, delay, TimeUnit.MILLISECONDS));
    }

    @Test
    void testGatherSchedulesOnce() {
      Plugin plugin = mock(Plugin.class);
      Server server = mock(Server.class);
      when(plugin.getServer()).thenReturn(server);
      // We use deep stubs so that whichever scheduler method used is stubbed as well.
      BukkitScheduler scheduler = mock(BukkitScheduler.class, RETURNS_DEEP_STUBS);
      when(server.getScheduler()).thenReturn(scheduler);

      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));

      List<String> batchData = List.of("a string", "various values", "Hello world!");
      for (String gatherable : batchData) {
        batch.add(gatherable);
      }

      verify(batch).schedule(any(Runnable.class));
    }


    @Test
    void testGatherContent() {
      var plugin = mock(Plugin.class);
      var server = mock(Server.class);
      when(plugin.getServer()).thenReturn(server);
      var scheduler = mock(BukkitScheduler.class, RETURNS_DEEP_STUBS);
      when(server.getScheduler()).thenReturn(scheduler);

      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
      var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
      doCallRealMethod().when(batch).schedule(runnableCaptor.capture());
      var postCaptor = ArgumentCaptor.forClass(Set.class);
      //noinspection unchecked
      doNothing().when(batch).post(postCaptor.capture());

      var gatherables = Set.of("a string", "various values", "Hello world!");

      for (String gatherable : gatherables) {
        batch.add(gatherable);
      }

      // Verify state and execute single batch.
      verify(batch).schedule(any(Runnable.class));
      Runnable task = runnableCaptor.getValue();
      assertDoesNotThrow(task::run);
      verify(batch).post(notNull());

      Set<?> postedBatch = postCaptor.getValue();
      assertThat(
          "Posted values must match expected values",
          postedBatch,
          both(hasSize(gatherables.size())).and(containsInAnyOrder(gatherables.toArray())));

      assertDoesNotThrow(task::run);
      verify(batch, times(2)).post(notNull());

      postedBatch = postCaptor.getValue();
      assertThat("Posted values must be empty", postedBatch, is(empty()));
    }

    @Test
    void testPurgeEmpty() {
      var plugin = mock(Plugin.class);
      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
      doNothing().when(batch).post(notNull());

      assertDoesNotThrow(batch::purge);
      verify(batch, times(0)).post(notNull());
    }

    @Test
    void testPurgeNullTask() {
      var plugin = mock(Plugin.class);
      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
      doNothing().when(batch).post(notNull());
      doReturn(null).when(batch).schedule(any(Runnable.class));

      batch.add("value");
      assertDoesNotThrow(batch::purge);
      verify(batch).post(notNull());
    }

    @Test
    void testPurgeCancelledTask() {
      var plugin = mock(Plugin.class);
      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
      doNothing().when(batch).post(notNull());
      var task = mock(BukkitTask.class);
      doReturn(true).when(task).isCancelled();
      doReturn(task).when(batch).schedule(any(Runnable.class));

      batch.add("value");
      assertDoesNotThrow(batch::purge);
      verify(task, times(0)).cancel();
      verify(batch).post(notNull());
    }

    @Test
    void testPurgeCancelsTask() {
      var plugin = mock(Plugin.class);
      Batch<String> batch = spy(ctor.apply(plugin, 50L, TimeUnit.MILLISECONDS));
      doNothing().when(batch).post(notNull());
      var task = mock(BukkitTask.class);
      doReturn(false).when(task).isCancelled();
      doReturn(task).when(batch).schedule(any(Runnable.class));

      batch.add("value");
      assertDoesNotThrow(batch::purge);
      verify(task, times(1)).cancel();
      verify(batch).post(notNull());
    }

  }

  @Nested
  class AsyncBatchTest extends BatchTest {

    AsyncBatchTest() {
      super((plugin, delay, delayUnit) -> new AsyncBatch<>(plugin, delay, delayUnit) {
        @Override
        protected void post(@NotNull @UnmodifiableView Set<String> batch) {
          // Ignore posted data.
        }
      });
    }

  }

  @Nested
  class SyncBatchTest extends BatchTest {

    SyncBatchTest() {
      super((plugin, delay, delayUnit) -> new SyncBatch<>(plugin, delay, delayUnit) {
        @Override
        protected void post(@NotNull @UnmodifiableView Set<String> batch) {
          // Ignore posted data.
        }
      });
    }

  }

}
