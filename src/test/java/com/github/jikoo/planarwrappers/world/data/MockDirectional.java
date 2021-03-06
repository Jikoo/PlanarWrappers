package com.github.jikoo.planarwrappers.world.data;

import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;

public class MockDirectional extends BlockDataMock implements Directional {

  private Set<BlockFace> directions = EnumSet.range(BlockFace.NORTH, BlockFace.DOWN);
  private BlockFace facing = BlockFace.NORTH;

  public MockDirectional(Material type) {
    super(type);
  }

  @Override
  public @NotNull BlockFace getFacing() {
    return this.facing;
  }

  @Override
  public void setFacing(@NotNull BlockFace facing) {
    if (!directions.contains(facing)) {
      throw new IllegalArgumentException("Facing not allowed!");
    }
    this.facing = facing;
  }

  @Override
  public @NotNull Set<BlockFace> getFaces() {
    return directions;
  }

  public void setDirections(@NotNull Set<BlockFace> directions) {
    this.directions = directions;
  }
}
