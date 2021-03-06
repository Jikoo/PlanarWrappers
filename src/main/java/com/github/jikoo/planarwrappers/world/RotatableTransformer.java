package com.github.jikoo.planarwrappers.world;

import org.bukkit.block.data.Rotatable;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockDataTransformer} for {@link Rotatable} {@link org.bukkit.block.data.BlockData}.
 */
public class RotatableTransformer extends SingleTransformer<Rotatable> {

  public RotatableTransformer(@NotNull Direction direction) {
    super(Rotatable.class, direction);
  }

  @Override
  protected void setData(@NotNull Rotatable data, @NotNull Direction rotated) {
    data.setRotation(rotated.toBlockFace());
  }
}
