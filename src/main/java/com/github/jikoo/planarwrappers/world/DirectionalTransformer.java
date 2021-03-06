package com.github.jikoo.planarwrappers.world;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockDataTransformer} for {@link Directional} {@link org.bukkit.block.data.BlockData}.
 */
public class DirectionalTransformer extends SingleTransformer<Directional> {

  public DirectionalTransformer(@NotNull Direction direction) {
    super(Directional.class, direction);
  }

  @Override
  public void setData(@NotNull Directional data, @NotNull Direction rotated) {
    BlockFace face = rotated.toBlockFace();
    if (data.getFaces().contains(face)) {
      data.setFacing(face);
    }
  }
}
