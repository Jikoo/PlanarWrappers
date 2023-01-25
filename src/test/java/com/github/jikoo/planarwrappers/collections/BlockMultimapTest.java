package com.github.jikoo.planarwrappers.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.github.jikoo.planarwrappers.mock.BukkitServer;
import com.github.jikoo.planarwrappers.mock.world.WorldMocks;
import com.github.jikoo.planarwrappers.tuple.Pair;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("Feature: Map multiple objects to blocks")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlockMultimapTest {

  World world;
  BlockMultimap<Object> blockMultimap;

  @BeforeAll
  void beforeAll() {
    Server server = BukkitServer.newServer();
    Bukkit.setServer(server);
    when(server.getWorld("world")).thenAnswer(invocation -> this.world);
    this.world = WorldMocks.newWorld("world");
  }

  @BeforeEach
  void beforeEach() {
    blockMultimap = new BlockMultimap<>();
  }

  @AfterAll
  void afterAll() {
    BukkitServer.unsetBukkitServer();
  }

  @DisplayName("Map entry set should contain expected content.")
  @Test
  void testEntries() {
    Multimap<Block, Object> normalMap = HashMultimap.create();
    Block block = world.getBlockAt(1, 1, 1);
    normalMap.put(block, "Oliver Horses");
    normalMap.put(block, "Oliver Mein");
    normalMap.put(block, "Humptydumpty Again");
    Block block1 = world.getBlockAt(2, 2, 2);
    normalMap.put(block1, "Anita Gnapp");
    normalMap.put(block1, "Slei Ping Tiem");

    normalMap.forEach(blockMultimap::put);

    // Google's Multimap EntrySet (and other things) appear to fail in comparison.
    List<Pair<Block, ArrayList<Object>>> entries =
        blockMultimap.entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), new ArrayList<>(entry.getValue())))
            .collect(Collectors.toList());
    List<Pair<Block, ArrayList<Object>>> values =
        normalMap.asMap().entrySet().stream()
            .map(entry -> new Pair<>(entry.getKey(), new ArrayList<>(entry.getValue())))
            .collect(Collectors.toList());

    assertThat(
        "Entries must match!",
        entries,
        both(everyItem(in(values))).and(containsInAnyOrder(values.toArray())));
  }

  @DisplayName("Map should support standard manipulation operations")
  @Test
  void testManipulate() {
    Block block = world.getBlockAt(0, 0, 0);
    Object object1 = "An object";
    Object object2 = "A different object";

    List<Object> value = Arrays.asList(object1, object2);

    assertThat("Block data should not be set beforehand", blockMultimap.get(block), nullValue());
    blockMultimap.put(block, object1);
    blockMultimap.put(block, object2);
    assertThat(
        "Value should be set",
        blockMultimap.get(block),
        both(everyItem(is(in(value)))).and(containsInAnyOrder(value.toArray())));
    assertThat(
        "Value should be removed",
        blockMultimap.remove(block),
        both(everyItem(is(in(value)))).and(containsInAnyOrder(value.toArray())));
    assertThat("Value should be null after removal", blockMultimap.get(block), nullValue());
    assertDoesNotThrow(() -> blockMultimap.remove(block));
  }

  @DisplayName("Map should support chunk-based manipulation")
  @Test
  void testManipulateChunk() {
    Block block1 = world.getBlockAt(0, 1, 0);

    Chunk chunk = block1.getChunk();

    String value1 = "value";
    blockMultimap.put(block1, value1);
    String value2 = "other value";
    blockMultimap.put(world.getBlockAt(15, 1, 15), value2);
    blockMultimap.put(world.getBlockAt(16, 1, 0), "different chunk value");
    blockMultimap.put(world.getBlockAt(-1, 1, 16), "another different chunk");

    Collection<Object> values = Arrays.asList(value1, value2);

    assertThat(
        "Correct values are returned for chunk",
        blockMultimap.get(chunk),
        both(everyItem(is(in(values)))).and(containsInAnyOrder(values.toArray())));

    assertThat(
        "Correct values are returned for chunk removal",
        blockMultimap.remove(chunk),
        both(everyItem(is(in(values)))).and(containsInAnyOrder(values.toArray())));

    assertThat("Block data should not be set after removal", blockMultimap.get(chunk), empty());
  }

}
