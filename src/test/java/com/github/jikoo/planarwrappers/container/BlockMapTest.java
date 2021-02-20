package com.github.jikoo.planarwrappers.container;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.block.BlockMock;
import com.github.jikoo.planarwrappers.util.Coords;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Feature: Map objects to blocks")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockMapTest {

  World world;
  BlockMap<Object> blockMap;

  @BeforeAll
  void beforeAll() {
    MockBukkit.mock();

    WorldMock world =
        new WorldMock() {
          @Override
          public Chunk getChunkAt(Block block) {
            return getChunkAt(Coords.blockToChunk(block.getX()), Coords.blockToChunk(block.getZ()));
          }

          @Override
          public Chunk getChunkAt(Location location) {
            return getChunkAt(
                Coords.blockToChunk(location.getBlockX()),
                Coords.blockToChunk(location.getBlockZ()));
          }
        };
    world.setName("world");
    MockBukkit.getMock().addWorld(world);

    this.world = world;
  }

  @BeforeEach
  void beforeEach() {
    blockMap = new BlockMap<>();
  }

  @DisplayName("Map should support standard manipulation operations")
  @Test
  void testManipulate() {
    Block block = new BlockMock(new Location(world, 0, 0, 0));
    Object object1 = "An object";
    Object object2 = "A different object";

    assertThat("Block data should not be set beforehand", blockMap.get(block), nullValue());
    assertThat("Previous value should be null", blockMap.put(block, object1), nullValue());
    assertThat("Value should be set", blockMap.get(block), is(object1));
    assertThat("Previous value should be returned", blockMap.put(block, object2), is(object1));
    assertThat("Value should be removed", blockMap.remove(block), is(object2));
  }

  @DisplayName("Map should support chunk-based manipulation")
  @Test
  void testManipulateChunk() {
    Block block1 = world.getBlockAt(0, 1, 0);

    Chunk chunk = block1.getChunk();

    assertThat("Block data should not be set beforehand", blockMap.get(chunk), empty());

    String value1 = "value";
    blockMap.put(block1, value1);
    String value2 = "other value";
    blockMap.put(world.getBlockAt(15, 1, 15), value2);
    blockMap.put(world.getBlockAt(16, 1, 0), "different chunk value");
    blockMap.put(world.getBlockAt(-1, 1, 16), "another different chunk");

    Collection<Object> values = Arrays.asList(value1, value2);

    assertThat(
        "Correct values are returned for chunk",
        blockMap.get(chunk),
        both(everyItem(is(in(values)))).and(containsInAnyOrder(values.toArray())));

    assertThat(
        "Correct values are returned for chunk removal",
        blockMap.remove(chunk),
        both(everyItem(is(in(values)))).and(containsInAnyOrder(values.toArray())));

    assertThat("Block data should not be set after removal", blockMap.get(chunk), empty());
  }

  @DisplayName("Map entry set should contain expected content.")
  @Test
  void testEntries() {
    Map<Block, Object> normalMap = new HashMap<>();
    normalMap.put(world.getBlockAt(1, 1, 1), "There once was a man from Nantucket,");
    normalMap.put(world.getBlockAt(2, 2, 2), "who tripped and fell in a bucket.");
    normalMap.put(world.getBlockAt(3, 3, 3), "His head hurt so badly");
    normalMap.put(world.getBlockAt(4, 4, 4), "as he sat there sadly");
    normalMap.put(world.getBlockAt(5, 5, 5), "he said to the bucket, aw CENSORED");

    normalMap.forEach(blockMap::put);
    Collection<Entry<Block, Object>> values = blockMap.entrySet();

    assertThat(
        "Entries must match!",
        normalMap.entrySet(),
        both(everyItem(is(in(values)))).and(containsInAnyOrder(values.toArray())));
  }

  @AfterAll
  void afterAll() {
    MockBukkit.unmock();
  }
}
