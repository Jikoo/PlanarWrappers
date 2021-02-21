package com.github.jikoo.planarwrappers.event;

import java.util.function.Consumer;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link EventExecutor} based on a provided {@link Consumer}.
 *
 * @param <T> the event type
 */
class ConsumerEventExecutor<T extends Event> implements EventExecutor {

  private final Class<T> eventClass;
  private final Consumer<T> eventConsumer;

  /**
   * Constructor for a new ConsumerEventExecutor.
   *
   * @param eventClass the class of the event
   * @param eventConsumer the consumer of the event
   */
  ConsumerEventExecutor(Class<T> eventClass, Consumer<T> eventConsumer) {
    this.eventClass = eventClass;
    this.eventConsumer = eventConsumer;
  }

  /**
   * Push event to Consumer.
   *
   * @param listener an ignored parameter
   * @param event the Event being called
   */
  @Override
  public void execute(@NotNull Listener listener, @NotNull Event event) {
    if (eventClass.isInstance(event)) {
      eventConsumer.accept(eventClass.cast(event));
    }
  }
}
