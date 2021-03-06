package com.github.jikoo.planarwrappers.world;

import org.bukkit.Axis;
import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockDataTransformer} for {@link Orientable} {@link org.bukkit.block.data.BlockData}.
 */
public class OrientableTransformer extends SingleTransformer<Orientable> {

  public OrientableTransformer(@NotNull Direction direction) {
    super(Orientable.class, direction);
  }

  protected void setData(@NotNull Orientable data, @NotNull Direction rotated) {
    Axis axis = rotated.toAxis();
    if (data.getAxes().contains(axis)) {
      data.setAxis(axis);
    }
  }
}
