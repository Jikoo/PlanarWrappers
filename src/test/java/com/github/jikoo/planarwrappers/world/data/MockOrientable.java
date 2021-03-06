package com.github.jikoo.planarwrappers.world.data;

import be.seeseemelk.mockbukkit.block.data.BlockDataMock;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.NotNull;

/**
 * Mock for Orientable BlockData.
 */
public class MockOrientable extends BlockDataMock implements Orientable {

  private Set<Axis> axes = EnumSet.allOf(Axis.class);
  private Axis axis = Axis.Y;

  public MockOrientable(Material type) {
    super(type);
  }

  @Override
  public @NotNull Axis getAxis() {
    return axis;
  }

  @Override
  public void setAxis(@NotNull Axis axis) {
    if (!this.axes.contains(axis)) {
      throw new IllegalArgumentException("Axis not allowed!");
    }
    this.axis = axis;
  }

  @Override
  public @NotNull Set<Axis> getAxes() {
    return axes;
  }

  public void setAxes(Set<Axis> axes) {
    this.axes = axes;
  }
}
