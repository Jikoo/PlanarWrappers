package com.github.jikoo.planarwrappers.world;

import org.bukkit.block.data.BlockData;

/**
 * An interface defining behavior for a {@link BlockData} transformer.
 */
public interface BlockDataTransformer {

  /**
   * Apply transformations to the given {@link BlockData}.
   *
   * @param blockData the {@code BlockData}
   * @param rotation the new orientation
   * @return true if the {@code BlockData} was affected
   */
  boolean transform(BlockData blockData, Direction rotation);
}
