package com.github.jikoo.planarwrappers.mock;

import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public final class ServerMocks {

  public static @NotNull Server newServer() {
    Server mock = mock(Server.class);

    Logger noOp = mock(Logger.class);
    when(mock.getLogger()).thenReturn(noOp);
    when(mock.isPrimaryThread()).thenReturn(true);

    // Horrible load order mess:
    // Modern faux-enums load their constants from their corresponding registry when initialized.
    // This happens as soon as the corresponding registry is constructed because the registry uses
    // the faux-enum class as an identifier in the loading process. This static loop means we must
    // be prepared to initialize everything before we can access either class.
    // As a bonus, these registries are wrapped in an Objects#requireNonNull call, so all must be
    // mocked to access any registry.
    doAnswer(invocationGetRegistry -> {
      Registry<?> registry = mock();
      doAnswer(invocationGetEntry -> {
        NamespacedKey key = invocationGetEntry.getArgument(0);
        // Set registries to always return a new value.
        // For Bukkit's faux-enums, this allows us to use their up-to-date namespaced keys instead
        // of maintaining a listing.
        Class<? extends Keyed> arg = invocationGetRegistry.getArgument(0);
        Keyed keyed = mock(arg);
        doReturn(key).when(keyed).getKey();
        return keyed;
      }).when(registry).get(notNull());
      return registry;
    }).when(mock).getRegistry(notNull());

    return mock;
  }

  public static void unsetBukkitServer() {
    try
    {
      Field server = Bukkit.class.getDeclaredField("server");
      server.setAccessible(true);
      server.set(null, null);
    }
    catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
    {
      throw new RuntimeException(e);
    }
  }

  private ServerMocks() {}

}
