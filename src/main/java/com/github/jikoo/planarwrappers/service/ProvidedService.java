package com.github.jikoo.planarwrappers.service;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.function.Supplier;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * A wrapper for an implementation provided by another {@link Plugin}. Gracefully handles classes
 * being loaded at runtime.
 *
 * <p>If your class requires listeners to be registered, you cannot rely on Bukkit's ServiceManager
 * to register them. Consider using the
 * {@link com.github.jikoo.planarwrappers.event.Event Event} utility or similar.
 *
 * <p>Implementations are not allowed to be generic. This is not strictly necessary in all cases,
 * but select usages prevent reification of the generic type when fetching service class.
 */
public abstract class ProvidedService<T> {

  protected final @NotNull Plugin plugin;
  private boolean setupDone = false;
  private @Nullable Wrapper<T> wrapper = null;

  protected ProvidedService(@NotNull Plugin plugin) {
    this.plugin = plugin;
    if (getClass().getTypeParameters().length > 0) {
      throw new IllegalStateException("ProvidedService may not be generic! For simplicity, make the class abstract.");
    }
  }

  public boolean isPresent() {
    return getService() != null;
  }

  protected final @Nullable Wrapper<T> getService() {
    // Attempt to initialize if not already set up.
    wrapClass(false);

    return wrapper;
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  final @Nullable Class<T> getServiceClass() {
    try {
      Type wrapperType = ProvidedService.class.getDeclaredField("wrapper").getGenericType();
      TypeToken<?> wrapperToken = TypeToken.of(getClass()).resolveType(wrapperType);
      Type genericType = wrapperToken.getRawType().getDeclaredField("wrappedObj").getGenericType();
      TypeToken<?> genericToken = wrapperToken.resolveType(genericType);
      return (Class<T>) genericToken.getRawType();
    } catch (TypeNotPresentException ignored) {
      // Class not present.
      return null;
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }

  protected final boolean isServiceImpl(@NotNull Class<?> clazz) {
    Class<?> serviceClass = getServiceClass();
    if (serviceClass == null) {
      return false;
    }

    return serviceClass.isAssignableFrom(clazz);
  }

  /**
   * Attempt to wrap provider. If the setup state does not match the provided value this does
   * nothing to prevent unnecessary loads.
   *
   * @param setupState the expected setup state
   */
  protected final void wrapClass(boolean setupState) {
    // If no change is likely, have we already obtained the service?
    if (setupState != setupDone) {
      return;
    }

    // Ensure service class is loaded.
    Class<T> clazz = getServiceClass();
    if (clazz == null) {
      finishSetup(null, logServiceClassNotLoaded());
      return;
    }

    T instance = getRegistration(clazz);

    // Ensure an instance is available.
    if (instance == null) {
      finishSetup(null, logNoProviderRegistered(clazz));
      return;
    }

    // If instance hasn't changed, do nothing.
    if (wrapper != null && wrapper.unwrap().equals(instance)) {
      return;
    }

    // Set setupDone false to log changing instance.
    setupDone = false;

    finishSetup(instance, logServiceProviderChange(clazz, instance));
  }

  protected abstract @Nullable T getRegistration(@NotNull Class<T> clazz);

  protected @Nullable Supplier<@NotNull String> logServiceClassNotLoaded() {
    return () -> "Service is not loaded, cannot use integration";
  }

  protected @Nullable Supplier<@NotNull String> logNoProviderRegistered(@NotNull Class<T> clazz) {
    return () -> "No provider available for " + clazz.getName();
  }

  protected @Nullable Supplier<String> logServiceProviderChange(
      @NotNull Class<T> clazz,
      @NotNull T instance) {
    return () -> "Hooked into " + clazz.getName() + " provider " + instance.getClass().getName();
  }

  /**
   * Do logging and set state for setup completion.
   *
   * @param value the new service to wrap
   * @param log a supplier for a logging line or null if no logging is to be done
   */
  private void finishSetup(@Nullable T value, @Nullable Supplier<String> log) {
    if (value == null) {
      wrapper = null;
    } else {
      wrapper = new Wrapper<>(value);
    }

    if (log != null && !setupDone) {
      plugin.getLogger().info(log);
    }

    setupDone = true;
  }

  /**
   * Wrapper class used to prevent Bukkit from logging an error and preventing registering events
   * for the listener when service class is not loaded.
   */
  public static class Wrapper<T> {

    private final @NotNull T wrappedObj;

    Wrapper(@NotNull T wrappedObj) {
      this.wrappedObj = wrappedObj;
    }

    public T unwrap() {
      return this.wrappedObj;
    }
  }

}
