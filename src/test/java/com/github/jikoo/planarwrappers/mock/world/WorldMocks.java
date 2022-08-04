package com.github.jikoo.planarwrappers.mock.world;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public final class WorldMocks {

  public static @NotNull World newWorld(@NotNull String name) {
    World mock = mock(World.class);

    when(mock.getName()).thenReturn(name);

    Map<Coordinate, Block> blocks = new HashMap<>();
    when(mock.getBlockAt(anyInt(), anyInt(), anyInt()))
        .thenAnswer(parameters ->
            blocks.computeIfAbsent(
                new Coordinate(parameters.getArgument(0), parameters.getArgument(1), parameters.getArgument(2)),
                key -> BlockMocks.newBlock(mock, key.x(), key.y(), key.z())));
    when(mock.getBlockAt(any(Location.class))).thenAnswer(parameters -> {
      Location location = parameters.getArgument(0);
      return mock.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    });

    return mock;
  }

  private record Coordinate(int x, int y, int z) {}

  private WorldMocks() {}

}
