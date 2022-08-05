package com.github.jikoo.planarwrappers.world;

import java.util.Set;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockDataTransformer} for {@link MultipleFacing} {@link org.bukkit.block.data.BlockData
 * BlockData}.
 */
public class MultipleFacingTransformer extends MultipleTransformer<MultipleFacing> {

  public MultipleFacingTransformer(@NotNull Set<Direction> faces) {
    super(MultipleFacing.class, faces);
  }

  @Override
  protected void setData(@NotNull MultipleFacing data, @NotNull Direction rotated) {
    BlockFace face = rotated.toBlockFace();
    if (data.getAllowedFaces().contains(face)) {
      data.setFace(face, true);
    }
  }
}
