package com.github.jikoo.planarwrappers.scheduler;

import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public abstract class SyncBatch<T> extends Batch<T> {

  protected SyncBatch(
      @NotNull Plugin plugin,
      long gatherPeriod,
      @NotNull TimeUnit gatherUnit) {
    super(plugin, gatherPeriod, gatherUnit);
  }

  @Override
  @NotNull BukkitTask schedule(@NotNull Runnable runnable) {
    return this.plugin.getServer().getScheduler()
        .runTaskLater(this.plugin, runnable, this.gatherTicks);
  }

}
