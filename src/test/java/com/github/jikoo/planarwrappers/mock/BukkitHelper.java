package com.github.jikoo.planarwrappers.mock;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;

public final class BukkitHelper {

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

  private BukkitHelper() {}

}
