package com.github.jikoo.planarwrappers.scheduler;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * A system for dividing operations up across multiple ticks to reduce server load. The total period
 * is used to form "buckets" of objects that are handled by a consumer each server tick.
 *
 * <p>This system is designed for tasks with a short overall period and many elements. It is not
 * optimized for periods longer than a few seconds, and may perform poorly in those situations.
 *
 * <p>The backing collection is a {@link Set}. Duplicate elements will be ignored.
 *
 * @param <T> the type of object stored
 */
public class DistributedTask<T> {

  private final Set<T> allContent = new HashSet<>();
  private final @NotNull Set<T> @NotNull [] distributedContent;
  private final @NotNull Consumer<Collection<T>> consumer;
  private int taskId = -1;
  private int currentIndex = 0;

  /**
   * Construct a new {@code DistributedTask}.
   *
   * @param period the total period
   * @param periodUnit the {@link TimeUnit} of the period
   * @param consumer the {@link Consumer} handling each bucket's contents
   * @throws IllegalArgumentException if the period is under 100MS (2 server ticks)
   */
  @SuppressWarnings("unchecked")
  public DistributedTask(
      long period,
      @NotNull TimeUnit periodUnit,
      @NotNull Consumer<@UnmodifiableView Collection<T>> consumer) {
    int totalTicks = (int) TickTimeUnit.toTicks(period, periodUnit);
    if (totalTicks < 2) {
      throw new IllegalArgumentException("Period must be 2 ticks or greater");
    }

    distributedContent = (Set<T>[]) Array.newInstance(allContent.getClass(), totalTicks);
    for (int index = 0; index < distributedContent.length; ++index) {
      distributedContent[index] = new HashSet<>();
    }

    this.consumer = consumer;
  }

  /**
   * Add an element.
   *
   * @param content the element to add
   */
  public void add(@NotNull T content) {
    if (!allContent.add(content)) {
      return;
    }

    int lowestSize = Integer.MAX_VALUE;
    int lowestIndex = 0;
    for (int index = 0; index < distributedContent.length; ++index) {
      int size = distributedContent[index].size();
      if (size < lowestSize) {
        lowestSize = size;
        lowestIndex = index;
      }
    }

    distributedContent[lowestIndex].add(content);
  }

  /**
   * Remove an element.
   *
   * @param content the element to remove
   */
  public void remove(@NotNull T content) {
    if (allContent.remove(content)) {
      for (Set<T> contentPartition : distributedContent) {
        if (contentPartition.remove(content)) {
          break;
        }
      }
    }
  }

  private void run() {
    consumer.accept(Collections.unmodifiableSet(distributedContent[currentIndex]));
    ++currentIndex;
    if (currentIndex >= distributedContent.length) {
      currentIndex = 0;
    }
  }

  /**
   * Schedule the task with the Bukkit scheduler.
   *
   * @param plugin the plugin scheduling the task
   * @return the same task instance
   */
  @Contract("_ -> this")
  public @NotNull DistributedTask<T> schedule(@NotNull Plugin plugin) {
    if (taskId != -1) {
      cancel(plugin);
    }

    taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::run, 1, 1);

    return this;
  }

  /**
   * Cancel the task scheduled with the Bukkit scheduler.
   *
   * @param plugin the plugin cancelling the task
   */
  public void cancel(@NotNull Plugin plugin) {
    if (taskId != -1) {
      plugin.getServer().getScheduler().cancelTask(taskId);
      taskId = -1;
    }
  }

}
