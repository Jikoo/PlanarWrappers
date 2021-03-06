package com.github.jikoo.planarwrappers.world.data;

import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;

/**
 * Mock for MultipleFacing BlockData.
 */
public class MockMultipleFacing extends BlockDataMock implements MultipleFacing {

  private Set<BlockFace> allowedFaces = EnumSet.range(BlockFace.NORTH, BlockFace.DOWN);
  private final Set<BlockFace> faces = EnumSet.noneOf(BlockFace.class);

  public MockMultipleFacing(Material material) {
    super(material);
  }

  @Override
  public boolean hasFace(@NotNull BlockFace face) {
    return this.faces.contains(face);
  }

  @Override
  public void setFace(@NotNull BlockFace face, boolean has) {
    if (!has) {
      this.faces.remove(face);
      return;
    }

    if (!this.allowedFaces.contains(face)) {
      throw new IllegalArgumentException("Facing not allowed!");
    }

    this.faces.add(face);
  }

  @NotNull
  @Override
  public Set<BlockFace> getFaces() {
    return Collections.unmodifiableSet(this.faces);
  }

  @NotNull
  @Override
  public Set<BlockFace> getAllowedFaces() {
    return this.allowedFaces;
  }

  public void setAllowedFaces(@NotNull Set<BlockFace> faces) {
    this.allowedFaces = faces;
  }
}
