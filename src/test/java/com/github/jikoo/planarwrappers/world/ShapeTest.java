package com.github.jikoo.planarwrappers.world;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.world.BlockDataMocks;
import com.github.jikoo.planarwrappers.mock.world.WorldMocks;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Define rotatable block-based assemblies.")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShapeTest {

  World world;

  @BeforeAll
  void beforeAll() {
    Server server = mock(Server.class);
    when(server.createBlockData(any(Material.class))).thenAnswer(parameters -> BlockDataMocks.newData(parameters.getArgument(0)));
    when(server.getLogger()).thenReturn(Logger.getLogger("bukkit"));
    Bukkit.setServer(server);
    world = WorldMocks.newWorld("world");
  }

  @DisplayName("Non-block material must throw exception")
  @Test
  void testNonBlockMat() {
    Shape shape = new Shape();
    assertThrows(
        IllegalArgumentException.class,
        () -> shape.set(0, 0, 0, Material.WOODEN_SWORD));
  }

  @DisplayName("Place blocks relative to key location by material")
  @ParameterizedTest
  @CsvSource({"NORTH", "EAST", "SOUTH", "WEST"})
  void testCreateBlockData(Direction direction) {
    Shape shape = new Shape();
    Material blockType = Material.ACACIA_PLANKS;
    shape.set(0, 0, 2, blockType);

    Block block = world.getBlockAt(10, 10, 10);
    shape.build(block, direction);
    Block relative = block.getRelative(direction.toBlockFace(), 2);
    assertThat("Relative block must be set correctly.", relative.getType(), is(blockType));
  }

  @DisplayName("Place blocks relative to key location")
  @ParameterizedTest
  @CsvSource({"NORTH", "EAST", "SOUTH", "WEST"})
  void testBlockRotation(Direction direction) {
    Shape shape = new Shape();
    Material blockType = Material.ACACIA_PLANKS;
    shape.set(0, 0, 2, createData(blockType, BlockDataMocks::newData));

    Block block = world.getBlockAt(10, 10, 10);
    shape.build(block, direction);
    Block relative = block.getRelative(direction.toBlockFace(), 2);
    assertThat("Relative block must be set correctly.", relative.getType(), is(blockType));
  }

  @DisplayName("Transform block data based on rotation")
  @ParameterizedTest
  @MethodSource("getTransforms")
  <T> void testTransformableRotation(
      Direction direction,
      TransformableBlockData data,
      Function<BlockData, T> getter,
      T expected) {
    Shape shape = new Shape();
    shape.set(0, 0, 0, data);
    Block block = world.getBlockAt(0, 0, 0);
    shape.build(block, direction);
    assertThat(
        "Data must be transformed as expected",
        getter.apply(block.getBlockData()),
        is(expected));
  }

  private Stream<Arguments> getTransforms() {
    int transformerCount = 4;
    Arguments[] arguments = new Arguments[Direction.values().length * transformerCount];
    for (Direction direction : Direction.values()) {
      int index = transformerCount * direction.ordinal();
      BlockFace face;
      Axis axis;
      switch (direction) {
        case NORTH -> {
          face = BlockFace.NORTH;
          axis = Axis.Z;
        }
        case EAST -> {
          face = BlockFace.EAST;
          axis = Axis.X;
        }
        case SOUTH -> {
          face = BlockFace.SOUTH;
          axis = Axis.Z;
        }
        case WEST -> {
          face = BlockFace.WEST;
          axis = Axis.X;
        }
        default -> throw new IllegalStateException("Unexpected direction");
      }
      arguments[index] = Arguments.of(
          direction,
          getMultipleFacing(),
          (Function<BlockData, Boolean>) data -> {
            MultipleFacing multiFacing = (MultipleFacing) data;
            return multiFacing.hasFace(face) && multiFacing.hasFace(face.getOppositeFace());
          },
          true);
      arguments[index + 1] = Arguments.of(
          direction,
          getOrientable(),
          (Function<BlockData, Axis>) data -> ((Orientable) data).getAxis(),
          axis);
      arguments[index + 2] = Arguments.of(
          direction,
          getDirectional(),
          (Function<BlockData, BlockFace>) data -> ((Directional) data).getFacing(),
          face);
      arguments[index + 3] = Arguments.of(
          direction,
          getRotatable(),
          (Function<BlockData, BlockFace>) data -> ((Rotatable) data).getRotation(),
          face);
    }
    return Arrays.stream(arguments);
  }

  private TransformableBlockData createData(
      Material material,
      Function<Material, BlockData> constructor) {
    return new TransformableBlockData(constructor.apply(material));
  }

  private TransformableBlockData getMultipleFacing() {
    return createData(Material.VINE, BlockDataMocks::multipleFacing)
        .withTransformer(
            new MultipleFacingTransformer(EnumSet.of(Direction.NORTH, Direction.SOUTH)));
  }

  private TransformableBlockData getOrientable() {
    return createData(Material.BONE_BLOCK, BlockDataMocks::orientable)
        .withTransformer(new OrientableTransformer(Direction.NORTH));
  }

  private TransformableBlockData getDirectional() {
    return createData(Material.SMOOTH_RED_SANDSTONE_STAIRS, BlockDataMocks::directional)
        .withTransformer(new DirectionalTransformer(Direction.NORTH));
  }

  private TransformableBlockData getRotatable() {
    return createData(Material.CRIMSON_WALL_SIGN, BlockDataMocks::rotatable)
        .withTransformer(new RotatableTransformer(Direction.NORTH));
  }
}
