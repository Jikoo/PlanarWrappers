package com.github.jikoo.planarwrappers.world;

import com.github.jikoo.planarwrappers.util.Generics;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * A base for a {@link BlockDataTransformer} that operates in multiple {@link Direction Directions}.
 *
 * @param <T> the type of {@link BlockData}
 */
public abstract class MultipleTransformer<T extends BlockData> implements BlockDataTransformer {

  private final Class<T> clazz;
  private final Set<Direction> faces;

  public MultipleTransformer(@NotNull Class<T> clazz, @NotNull Set<Direction> faces) {
    this.clazz = clazz;
    this.faces = EnumSet.copyOf(faces);
  }

  @Override
  public boolean transform(@NotNull BlockData blockData, @NotNull Direction rotation) {
    return Generics.consumeAs(
        clazz,
        blockData,
        data -> faces.forEach(direction -> setData(data, direction.getRelativeDirection(rotation))));
  }

  protected abstract void setData(T data, Direction rotated);
}
