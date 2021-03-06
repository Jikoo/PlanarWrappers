package com.github.jikoo.planarwrappers.world.data;

import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import java.util.EnumSet;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;

/**
 * Mock for Rotatable BlockData.
 */
public class MockRotatable extends BlockDataMock implements Rotatable {

  private static final EnumSet<BlockFace> ROTATIONS = EnumSet.range(BlockFace.NORTH, BlockFace.DOWN);

  private BlockFace rotation;

  public MockRotatable(Material type) {
    super(type);
    this.rotation = BlockFace.NORTH;
  }

  @Override
  public @NotNull BlockFace getRotation() {
    return rotation;
  }

  @Override
  public void setRotation(@NotNull BlockFace rotation) {
    if (!ROTATIONS.contains(rotation)) {
      throw new IllegalArgumentException("Rotatable may only face an axis!");
    }
    this.rotation = rotation;
  }
}
