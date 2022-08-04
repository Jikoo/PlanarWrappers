package com.github.jikoo.planarwrappers.world;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container for a BlockData that may later be transformed directionally.
 */
public class TransformableBlockData {

  private final @NotNull BlockData blockData;
  private @Nullable List<BlockDataTransformer> transformers;

  public TransformableBlockData(@NotNull Material material) {
    this.blockData = material.createBlockData();
    this.transformers = null;
  }

  public TransformableBlockData(@NotNull BlockData blockData) {
    this.blockData = blockData;
    this.transformers = null;
  }

  public TransformableBlockData withTransformer(@NotNull BlockDataTransformer transform) {
    if (transformers == null) {
      transformers = new ArrayList<>();
    }
    transformers.add(transform);
    return this;
  }

  public @NotNull BlockData getTransformedData(@NotNull Direction direction) {
    if (this.transformers == null) {
      return this.blockData;
    }
    BlockData data = this.blockData.clone();
    this.transformers.forEach(transformer -> transformer.transform(data, direction));
    return data;
  }
}
