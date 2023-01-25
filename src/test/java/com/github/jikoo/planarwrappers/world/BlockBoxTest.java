package com.github.jikoo.planarwrappers.world;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.jikoo.planarwrappers.function.TriConsumer;
import com.github.jikoo.planarwrappers.mock.world.WorldMocks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Feature: Define block-based bounding boxes")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockBoxTest {

  World world;

  @BeforeAll
  void beforeAll() {
    this.world = WorldMocks.newWorld("world");
  }

  @DisplayName("Bounding box must produce expected min/max values.")
  @Test
  void testMinMax() {
    int minX = -5;
    int minY = 0;
    int minZ = -5;
    int maxX = 5;
    int maxY = 10;
    int maxZ = 5;

    BlockBox result = new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);

    assertThat("Min X must match", result.getMinX(), is(minX));
    assertThat("Min Y must match", result.getMinY(), is(minY));
    assertThat("Min Z must match", result.getMinZ(), is(minZ));
    assertThat("Min Vector must match", result.getMin(), is(new Vector(minX, minY, minZ)));
    assertThat("Max X must match", result.getMaxX(), is(maxX));
    assertThat("Max Y must match", result.getMaxY(), is(maxY));
    assertThat("Max Z must match", result.getMaxZ(), is(maxZ));
    assertThat("Max Vector must match", result.getMax(), is(new Vector(maxX, maxY, maxZ)));
  }

  @DisplayName("Bounding box must verify min/max coordinates on construction")
  @Test
  void testVerify() {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 10, 10);
    BlockBox boxB = new BlockBox(10, 10, 10, 0, 0, 0);
    assertThat("Verified results must be equal", boxA, is(boxB));
  }

  @DisplayName("Bounding box creation must produce consistent results")
  @Test
  void testCreate() {
    BlockBox expected = new BlockBox(0, 0, 0, 0, 0, 0, false);

    BlockBox result = new BlockBox(world.getBlockAt(0, 0, 0));
    assertThat("Block bounding box must match", result, is(expected));

    result = new BlockBox(org.bukkit.util.BoundingBox.of(world.getBlockAt(0, 0, 0)));
    assertThat("Degenerate Bukkit bounding box must match", result, is(expected));

    result = new BlockBox(new org.bukkit.util.BoundingBox(0, 0, 0, 0, 0, 0));
    assertThat("Degenerate Bukkit bounding box must match", result, is(expected));

    int minX = -5;
    int minY = 0;
    int minZ = -5;
    int maxX = 5;
    int maxY = 10;
    int maxZ = 5;

    expected = new BlockBox(minX, minY, minZ, maxX, maxY, maxZ, false);

    result = new BlockBox(minX, minY, minZ, maxX, maxY, maxZ);
    assertThat("Verified bounding box must match", result, is(expected));

    result = new BlockBox(new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
    assertThat("Verified location bounding box must match", result, is(expected));

    result =
        new BlockBox(new Location(null, minX, minY, minZ), new Location(null, maxX, maxY, maxZ));
    assertThat("Verified vector bounding box must match", result, is(expected));

    // Bukkit bounding boxes are exclusive of max corner, construct with max + 1.
    result =
        new BlockBox(
            new org.bukkit.util.BoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1));
    assertThat("Bukkit bounding box must match", result, is(expected));

    List<Block> blocks = new ArrayList<>();
    for (int x = minX; x <= maxX; ++x) {
      for (int y = minY; y <= maxY; ++y) {
        for (int z = minZ; z <= maxZ; ++z) {
          blocks.add(world.getBlockAt(x, y, z));
        }
      }
    }

    result = BlockBox.ofBlocks(blocks);
    assertThat("Verified location bounding box must match", result, is(expected));

    List<Block> noBlocks = Collections.emptyList();
    assertThrows(IllegalArgumentException.class, () -> BlockBox.ofBlocks(noBlocks));
  }

  @DisplayName("Bounding box must produce expected measurements.")
  @Test
  void testMeasurements() {
    BlockBox boxA = new BlockBox(-1, 0, 1, 5, 4, 3);
    assertThat("Length must match", boxA.getLength(), is(7));
    assertThat("Height must match", boxA.getHeight(), is(5));
    assertThat("Width must match", boxA.getWidth(), is(3));
    assertThat("Area must match", boxA.getArea(), is(7 * 3));
    assertThat("Volume must match", boxA.getVolume(), is(7 * 3 * 5));
    assertThat("Center X must match", boxA.getCenterX(), is(2.5));
    assertThat("Center Y must match", boxA.getCenterY(), is(2.5));
    assertThat("Center Z must match", boxA.getCenterZ(), is(2.5));
    assertThat("Center Vector must match", boxA.getCenter(), is(new Vector(2.5, 2.5, 2.5)));
  }

  @DisplayName("Bounding box must be copiable.")
  @ParameterizedTest
  @MethodSource("getCopyFunctions")
  void testCopy(Function<BlockBox, BlockBox> copy) {
    BlockBox boxA = new BlockBox(1, 2, 3, 4, 5, 6);
    BlockBox boxB = copy.apply(boxA);
    assertThat("Min X must match", boxB.getMinX(), is(boxA.getMinX()));
    assertThat("Min Y must match", boxB.getMinY(), is(boxA.getMinY()));
    assertThat("Min Z must match", boxB.getMinZ(), is(boxA.getMinZ()));
    assertThat("Max X must match", boxB.getMaxX(), is(boxA.getMaxX()));
    assertThat("Max Y must match", boxB.getMaxY(), is(boxA.getMaxY()));
    assertThat("Max Z must match", boxB.getMaxZ(), is(boxA.getMaxZ()));
  }

  private Stream<Arguments> getCopyFunctions() {
    return Arrays.stream(
        new Arguments[] {
          Arguments.of(
              (Function<BlockBox, BlockBox>)
                  box -> {
                    BlockBox boxB = new BlockBox(7, 8, 9, 10, 11, 12);
                    boxB.copy(box);
                    return box;
                  }),
          Arguments.of((Function<BlockBox, BlockBox>) BlockBox::clone)
        });
  }

  @DisplayName("Bounding box must be resizable.")
  @Test
  void testResize() {
    testBlockfaceFunction(BlockBox::resize, new BlockBox(0, 0, -10, 10, 10, 10));
  }

  private void testBlockfaceFunction(
      TriConsumer<BlockBox, BlockFace, Integer> function, BlockBox boxB) {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 10, 10);
    function.accept(boxA, BlockFace.NORTH, 10);
    assertThat("Initial NORTH function must match", boxA, is(boxB));

    for (BlockFace face : BlockFace.values()) {
      if (face == BlockFace.SELF) {
        function.accept(boxA, face, 15);
        assertThat("SELF function must match", boxA, is(boxB));
        continue;
      }
      function.accept(boxA, face, 15);
      assertThat(face.name() + " function must not match", boxA, is(not(boxB)));
      function.accept(boxA, face, -15);
      assertThat(face.name() + " revert must match", boxA, is(boxB));
    }
  }

  @DisplayName("Bounding box must be moveable.")
  @Test
  void testMove() {
    testBlockfaceFunction(BlockBox::move, new BlockBox(0, 0, -10, 10, 10, 0));
  }

  @DisplayName("Bounding box must expand to contain new points.")
  @Test
  void testUnion() {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 10, 10);

    boxA.union(0, 15, 20);
    BlockBox boxB = new BlockBox(0, 0, 0, 10, 15, 20);
    assertThat("Point union must match expected values", boxA, is(boxB));

    boxA.union(world.getBlockAt(-10, 7, 10));
    boxB = new BlockBox(-10, 0, 0, 10, 15, 20);
    assertThat("Block union must match expected values", boxA, is(boxB));

    boxA.union(new Vector(20, 10, -5));
    boxB = new BlockBox(-10, 0, -5, 20, 15, 20);
    assertThat("Vector union must match expected values", boxA, is(boxB));

    boxA.union(new Location(null, 20, 20, 20));
    boxB = new BlockBox(-10, 0, -5, 20, 20, 20);
    assertThat("Location union must match expected values", boxA, is(boxB));

    boxA.union(new BlockBox(-10, -10, -10, 5, 5, 5));
    boxB = new BlockBox(-10, -10, -10, 20, 20, 20);
    assertThat("BlockBox union must match expected values", boxA, is(boxB));
  }

  @DisplayName("Bounding boxes must intersect if corners overlap.")
  @Test
  void testIntersectCorner() {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 0, 10);
    BlockBox boxB = new BlockBox(5, 0, 5, 15, 0, 15);
    BlockBox boxC = new BlockBox(-5, 0, -5, 4, 0, 4);

    assertThat("Box A intersects Box B", boxA.intersects(boxB));
    assertThat("Box B intersects box A", boxB.intersects(boxA));
    assertThat("Box A intersects box C", boxA.intersects(boxC));
    assertThat("Box C intersects box A", boxC.intersects(boxA));
    assertThat("Box B does not intersect box C", !boxB.intersects(boxC));
    assertThat("Box C does not intersect box B", !boxC.intersects(boxB));
  }

  @DisplayName("Bounding boxes must intersect if centers overlap.")
  @Test
  void testIntersectCenter() {
    BlockBox boxA = new BlockBox(0, 0, 5, 10, 0, 15);
    BlockBox boxB = new BlockBox(5, 0, 0, 15, 0, 10);

    assertThat("Box A intersects box B", boxA.intersects(boxB));
    assertThat("Box B intersects box A", boxB.intersects(boxA));
  }

  @DisplayName("Bounding boxes must not intersect if linearly adjacent.")
  @Test
  void testIntersectLinearAdjacent() {
    // Linear North-South
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 0, 10);
    BlockBox boxB = new BlockBox(0, 0, 11, 10, 0, 21);
    BlockBox boxC = new BlockBox(0, 0, 10, 10, 0, 20);

    // Adjacent
    assertThat("Box A does not intersect box B", !boxA.intersects(boxB));
    assertThat("Box B does not intersect box A", !boxB.intersects(boxA));
    // Overlapping on edge
    assertThat("Box A intersects box C", boxA.intersects(boxC));
    assertThat("Box C intersects box A", boxC.intersects(boxA));

    // Linear East-West
    boxA = new BlockBox(0, 0, 0, 10, 0, 10);
    boxB = new BlockBox(11, 0, 0, 21, 0, 10);
    boxC = new BlockBox(10, 0, 0, 20, 0, 10);

    // Adjacent
    assertThat("Box A does not intersect box B", !boxA.intersects(boxB));
    assertThat("Box B does not intersect box A", !boxB.intersects(boxA));
    // Overlapping on edge
    assertThat("Box A intersects box C", boxA.intersects(boxC));
    assertThat("Box C intersects box A", boxC.intersects(boxA));
  }

  @DisplayName("Bounding box must report points within their area as contained two-dimensionally.")
  @Test
  void testContains2d() {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 10, 10);
    assertThat("Points within area must be contained", boxA.contains2d(5, 5));

    Vector vector = new Vector(1, -1, 1);
    assertThat("Point within area must be contained ignoring vertical", boxA.contains2d(vector));
    assertThat("Point within area must be not be contained outside box", !boxA.contains(vector));

    Location location = new Location(null, 10, 50, 10);
    assertThat("Point within area must be contained ignoring vertical", boxA.contains2d(location));
    assertThat("Point within area must be not be contained outside box", !boxA.contains(location));

    Block block = world.getBlockAt(8, 11, 7);
    assertThat("Point within area must be contained ignoring vertical", boxA.contains2d(block));
    assertThat("Point within area must be not be contained outside box", !boxA.contains(block));

    BlockBox boxB = boxA.clone();
    boxB.move(BlockFace.UP, 20);

    assertThat("Box A does not contain box B", !boxA.contains(boxB));
    assertThat("Box A contains box B's area", boxA.contains2d(boxB));
    assertThat("Box A contains box B's area", boxA.contains2d(boxB));

    assertThat(
        "Box defined by points must be contained",
        boxA.contains2d(boxB.getMinX(), boxB.getMinZ(), boxB.getMaxX(), boxB.getMaxZ()));
    assertThat(
        "Box defined by points must be contained",
        boxA.contains2d(boxB.getMaxX(), boxB.getMaxZ(), boxB.getMinX(), boxB.getMinZ()));
  }

  @DisplayName("Bounding box must report points within their area as contained.")
  @Test
  void testContainment() {
    BlockBox boxA = new BlockBox(0, 0, 0, 10, 10, 10);

    assertThat("Points within area must be contained", boxA.contains(5, 5, 5));
    assertThat("Points outside area must not be contained", !boxA.contains(11, 5, 5));
    assertThat("Points outside area must not be contained", !boxA.contains(5, 11, 5));
    assertThat("Points outside area must not be contained", !boxA.contains(5, 5, 11));
    assertThat("Points outside area must not be contained", !boxA.contains(-1, 5, 5));
    assertThat("Points outside area must not be contained", !boxA.contains(5, -1, 5));
    assertThat("Points outside area must not be contained", !boxA.contains(5, 5, -1));

    Vector vector = new Vector(1, 1, 1);
    assertThat("Point within area must be contained", boxA.contains(vector));

    Location location = new Location(null, 10, 10, 10);
    assertThat("Point within area must be contained", boxA.contains(location));

    Block block = world.getBlockAt(8, 4, 7);
    assertThat("Point within area must be contained", boxA.contains(block));

    // Complete containment
    boxA = new BlockBox(0, 0, 0, 20, 0, 20);
    BlockBox boxB = new BlockBox(5, 0, 5, 15, 0, 15);
    BlockBox boxC = new BlockBox(-5, 0, -5, 4, 0, 4);
    BlockBox boxD = boxA.clone();

    assertThat("Box A contains box B", boxA.contains(boxB));
    assertThat("Box B intersects box A", boxB.intersects(boxA));
    assertThat("Box B does not contain box A", !boxB.contains(boxA));
    assertThat("Box A does not contain box C", !boxA.contains(boxC));
    assertThat("Equal boxes contain each other", boxA.contains(boxD));
    assertThat("Equal boxes contain each other", boxD.contains(boxA));

    assertThat(
        "Box defined by points must be contained",
        boxA.contains(
            boxB.getMinX(),
            boxB.getMinY(),
            boxB.getMinZ(),
            boxB.getMaxX(),
            boxB.getMaxY(),
            boxB.getMaxZ()));
    assertThat(
        "Box defined by points must be contained",
        boxA.contains(
            boxB.getMaxX(),
            boxB.getMaxY(),
            boxB.getMaxZ(),
            boxB.getMinX(),
            boxB.getMinY(),
            boxB.getMinZ()));
  }

  @DisplayName("Bounding box intersection must be overlapping area.")
  @ParameterizedTest
  @MethodSource("getIntersections")
  void testIntersection(BlockBox boxA, BlockBox boxB, BlockBox result) {
    assertThat(
        "Bounding box intersection must match expected area", boxA.intersection(boxB), is(result));
  }

  private Stream<Arguments> getIntersections() {
    return Arrays.stream(
        new Arguments[] {
          Arguments.of(
              new BlockBox(-5, -5, -5, 5, 5, 5),
              new BlockBox(0, 0, 0, 10, 10, 10),
              new BlockBox(0, 0, 0, 5, 5, 5)),
          Arguments.of(new BlockBox(-5, -5, -5, -1, -1, -1), new BlockBox(1, 1, 1, 5, 5, 5), null)
        });
  }

}
