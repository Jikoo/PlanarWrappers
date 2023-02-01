package com.github.jikoo.planarwrappers.scheduler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * Aggregate multiple elements for handling later. Helps reduce redundant runnable/thread creation.
 *
 * @param <T>
 */
public abstract class Batch<T> {

  private final @NotNull Set<T> batch = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final @NotNull AtomicReference<BukkitTask> task = new AtomicReference<>();
  final @NotNull Plugin plugin;
  final long gatherTicks;

  protected Batch(@NotNull Plugin plugin, long gatherPeriod, @NotNull TimeUnit gatherUnit) {
    this.plugin = plugin;
    this.gatherTicks = TickTimeUnit.toTicks(gatherPeriod, gatherUnit);
    if (gatherTicks <= 0) {
      throw new IllegalArgumentException("Gather ticks must be > 0");
    }
  }

  public void add(@NotNull T element) {
    this.batch.add(element);
    this.trySchedule();
  }

  private void trySchedule() {
    if (this.task.get() != null || this.batch.isEmpty()) {
      return;
    }

    this.task.set(this.schedule(this::run));
  }

  abstract @NotNull BukkitTask schedule(@NotNull Runnable runnable);

  private void run() {
    // Copy all elements to a new set, clearing original in the process.
    Set<T> localBatch = new HashSet<>(this.batch.size());
    this.batch.removeIf(element -> {
      localBatch.add(element);
      return true;
    });

    // Unset task.
    this.task.set(null);

    // Ensure we don't miss new elements added during time between clearing and unsetting.
    if (!this.batch.isEmpty()) {
      this.trySchedule();
    }

    // Post results to the batch consumer.
    this.post(Collections.unmodifiableSet(localBatch));
  }

  protected abstract void post(@NotNull @UnmodifiableView Set<T> batch);

  public void purge() {
    if (batch.isEmpty()) {
      return;
    }

    var batchCopy = Set.copyOf(this.batch);
    this.task.getAndUpdate(task -> {
      if (task != null && !task.isCancelled()) {
        task.cancel();
      }
      return null;
    });
    this.batch.clear();

    this.post(batchCopy);
  }

}
