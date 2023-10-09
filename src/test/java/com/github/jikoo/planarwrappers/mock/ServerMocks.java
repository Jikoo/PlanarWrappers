package com.github.jikoo.planarwrappers.mock;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.mockito.ArgumentMatchers;

public final class ServerMocks {

  public static @NotNull Server newServer() {
    Server mock = mock(Server.class);

    Logger noOp = mock(Logger.class);
    when(mock.getLogger()).thenReturn(noOp);
    when(mock.isPrimaryThread()).thenReturn(true);

    doAnswer(invocation -> mock(Registry.class)).when(mock).getRegistry(ArgumentMatchers.notNull());

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
