package com.github.jikoo.planarwrappers.event;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

/**
 * A utility for managing {@link Consumer}-based {@link Listener Listeners} for {@link Event
 * Events}.
 */
public class Event {

  private Event() {}

  /**
   * Register to consume an Event.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param <T> the type of Event
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass, @NotNull Consumer<T> consumer, @NotNull Plugin plugin) {
    register(eventClass, consumer, plugin, EventPriority.NORMAL);
  }

  /**
   * Register to consume an Event.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param priority the EventPriority to register at
   * @param <T> the type of Event
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull EventPriority priority) {
    register(eventClass, consumer, plugin, priority, true);
  }

  /**
   * Register to consume an Event.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param priority the EventPriority to register at
   * @param ignoreCancelled whether cancelled events should be skipped
   * @param <T> the type of Event
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull EventPriority priority,
      boolean ignoreCancelled) {
    HandlerList handlerList = getHandlerList(eventClass);

    handlerList.register(
        new RegisteredListener(
            new Listener() {},
            new ConsumerEventExecutor<>(eventClass, consumer),
            priority,
            plugin,
            ignoreCancelled));
  }

  /**
   * Register to consume an Event using a specific key.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param key the key to be used
   * @param <T> the type of Event
   * @see #register(Class, Consumer, Plugin, String, EventPriority, boolean)
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull String key) {
    register(eventClass, consumer, plugin, key, EventPriority.NORMAL);
  }

  /**
   * Register to consume an Event using a specific key.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param key the key to be used
   * @param priority the EventPriority to register at
   * @param <T> the type of Event
   * @see #register(Class, Consumer, Plugin, String, EventPriority, boolean)
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull String key,
      @NotNull EventPriority priority) {
    register(eventClass, consumer, plugin, key, priority, true);
  }

  /**
   * Register to consume an Event using a specific key.
   *
   * <p>Note that keyed listeners are unique and operate on a first-come-first-served basis. If a
   * keyed listener with an identical key exists, the new listener is ignored. To override an
   * existing keyed listener, remove it with {@link #unregister(Class, String)} first.
   *
   * <p>If you want to register a single unique keyed listener across multiple plugins, you'll need
   * to ensure that the same KeyedListener class is used by both plugin instances. Identical
   * relocation is not sufficient due to how plugin classloading works. The classloader searches
   * first the plugin, then the server, then the dependencies of the plugin, and finally the other
   * present plugins - this means that while classes may have identical names and packaging, they
   * may have been loaded by completely different classloaders.
   *
   * @param eventClass the Class of the Event to consume
   * @param consumer the Consumer
   * @param plugin the Plugin registering the Consumer
   * @param key the key to be used
   * @param priority the EventPriority to register at
   * @param ignoreCancelled whether cancelled events should be skipped
   * @param <T> the type of Event
   */
  public static <T extends org.bukkit.event.Event> void register(
      @NotNull Class<T> eventClass,
      @NotNull Consumer<T> consumer,
      @NotNull Plugin plugin,
      @NotNull String key,
      @NotNull EventPriority priority,
      boolean ignoreCancelled) {
    HandlerList handlerList = getHandlerList(eventClass);

    for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
      if (registeredListener instanceof KeyedListener
          && ((KeyedListener<?>) registeredListener).getKey().equals(key)) {
        return;
      }
    }

    handlerList.register(
        new KeyedListener<>(eventClass, consumer, plugin, key, priority, ignoreCancelled));
  }

  /**
   * Unregister a KeyedListener by key.
   *
   * @param eventClass the Class of Event
   * @param key the key
   * @param <T> the type of Event
   */
  public static <T extends org.bukkit.event.Event> void unregister(
      @NotNull Class<T> eventClass, @NotNull String key) {
    HandlerList handlerList = getHandlerList(eventClass);

    for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
      if (registeredListener instanceof KeyedListener
          && ((KeyedListener<?>) registeredListener).getKey().equals(key)) {
        // Safe to unregister
        handlerList.unregister(registeredListener);
        return;
      }
    }
  }

  /**
   * Get the HandlerList for an Event.
   *
   * @param clazz the type of Event
   * @param <T> the type of Event
   * @return the HandlerList
   * @throws IllegalStateException if a HandlerList cannot be obtained from the event
   */
  private static <T extends org.bukkit.event.Event> HandlerList getHandlerList(
      @NotNull Class<T> clazz) {
    try {
      Method getHandlers = clazz.getMethod("getHandlerList");
      Object handlerListObj = getHandlers.invoke(null);
      return (HandlerList) handlerListObj;
    } catch (Exception e) {
      // Re-throw exception - should only happen if event is missing required method.
      throw new IllegalArgumentException(
          "Event " + clazz.getName() + " does not declare a static getHandlerList method!", e);
    }
  }
}
