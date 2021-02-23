package com.github.jikoo.planarwrappers.event;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

/**
 * A listener containing a key.
 *
 * <p>This class is visible purely so plugins can decide what to do with specific listeners. It is
 * not supposed to be instantiated by any means other than registration via the {@link
 * com.github.jikoo.planarwrappers.event.Event Event} utility.
 *
 * @param <T> the type of event being listened to
 */
public class KeyedListener<T extends Event> extends RegisteredListener {

  private final String key;

  /**
   * Constructor for a new KeyedListener.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param key the key to be used
   * @param priority the EventPriority to register at
   * @param ignoreCancelled whether cancelled events should be skipped
   */
  KeyedListener(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull String key,
      @NotNull EventPriority priority,
      boolean ignoreCancelled) {
    super(
        new Listener() {},
        new ConsumerEventExecutor<>(eventClass, consumer),
        priority,
        plugin,
        ignoreCancelled);
    this.key = key;
  }

  /**
   * Get the key used by this listener.
   *
   * @return the key
   */
  public String getKey() {
    return this.key;
  }
}
