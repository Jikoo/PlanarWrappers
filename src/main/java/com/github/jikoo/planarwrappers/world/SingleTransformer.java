package com.github.jikoo.planarwrappers.world;

import com.github.jikoo.planarwrappers.util.Generics;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A base for a {@link BlockDataTransformer} that operates in a single {@link Direction}.
 *
 * @param <T> the type of {@link BlockData}
 */
public abstract class SingleTransformer<T extends BlockData> implements BlockDataTransformer {

  private final Class<T> clazz;
  private final Direction direction;

  protected SingleTransformer(@NotNull Class<T> clazz, @NotNull Direction direction) {
    this.clazz = clazz;
    this.direction = direction;
  }

  @Override
  public boolean transform(BlockData blockData, Direction rotation) {
    return Generics.consumeAs(
        clazz,
        blockData,
        data -> setData(data, direction.getRelativeDirection(rotation))
    );
  }

  protected abstract void setData(T data, Direction rotated);
}
