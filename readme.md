# PlanarWrappers

[![Build](https://github.com/Jikoo/PlanarWrappers/actions/workflows/ci.yml/badge.svg)](https://github.com/Jikoo/PlanarWrappers/actions/workflows/ci.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Jikoo_PlanarWrappers&metric=coverage)](https://sonarcloud.io/dashboard?id=Jikoo_PlanarWrappers)  
Yet another Bukkit-related library.

## About

I keep re-using code I've written across projects. It's hard to remember which files are shared,
which projects have and have not had changes ported to them, etc. By creating a unified utility
library, I hope to increase the robustness of my plugins and ease my own workload.

## Offerings

### Simplified per-world settings

Per-world settings are a staple of user-configurability. Unfortunately, they also come with the
major drawback of requiring you to do a lot of work handling falling through to defaults. In
addition, many times the data type you're using isn't directly supported by Bukkit or SnakeYaml.
After several years of blindly fumbling through configuration work, I've developed a system that I'm
relatively satisfied with.

* `Setting`: A generic overridable setting. Provided implementations include primitives and several
  basic Bukkit ConfigurationSerializable objects.
* `Mapping`: A generic overridable mapping. Unlike a `Setting<Map>`, keys fall through to values for
  default settings.  
  Sample `Setting<Map>` vs `Mapping` YAML:
  ```yaml
  my_mapping:
    a: 1
    b: 2
  overrides:
    world_b:
      my_mapping:
        b: 1
  ```
  Assume all values not present are always `0`.  
  The value for `my_mapping` with key `a` in world `world_b` would be `1` because it is declared in
  the default `my_mapping` values.  
  Were this a `Setting<Map>`, the key `a` would not be present in the override `world_b` and would
  fall through to `0`.

### Graceful optional dependency handling

How many times have we all written a Vault hook? It's easy to do quickly, but can take a while to
do properly. Not only do the `VaultEconomy` and `VaultPermission` provide these hooks, the
`ManagerProvidedService` (or `PluginProvidedService` for specific plugins) makes it easy to properly
handle classes not being loaded at runtime without the usual nightmare of breaking reflective
access to the caller entirely. No more failure to register events because you imported a class
that doesn't exist!

### Lambda-based event registration

Speaking of failures to register events, what if you didn't need to rely on Bukkit to reflectively
register your events and could instead use an event consumer? The `Event` utility provides this.

### Task management

If you have a repeating task with a period under a few seconds ticking multiple entries, it may
be safer to divide the load across the ticks of the whole period to prevent spikes in tick time.
`DistributedTask` provides automatic bucketing of entries.

Alternately, for cases where multiple tasks might be scheduled in quick succession, the
`AsyncBatch` and `SyncBatch` provide an easy way to wait for a short period before pushing
a group of changes. This may help reduce I/O by reducing repeated writes to the same file or
improve database connection reuse by clustering calls to be submitted in a batch.

### Other Useful Stuff

* Version comparisons
* Functions - throwing functions, trifunction/triconsumer
* Experience conversion and management
* Convert coordinates between region, chunk, and block values
* Weighted random selection
* Etc.

## Dependency Information

PlanarWrappers is available via [JitPack](https://jitpack.io). JitPack supports Gradle, Maven, SBT, and
Leiningen.

### Maven

Replace `${versions.planar}` with the version you desire to work with. The `minimizeJar` option is
recommended to prevent inflating your plugin with unnecessary classes.  

Sample configuration:

```xml
<project>
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>com.github.jikoo</groupId>
      <artifactId>planarwrappers</artifactId>
      <version>${versions.planar}</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>${versions.shade}</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <minimizeJar>true</minimizeJar>
              <relocations>
                <relocation>
                  <pattern>com.github.jikoo.planarwrappers</pattern>
                  <shadedPattern>com.example.myplugin.planarwrappers</shadedPattern>
                </relocation>
              </relocations>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```
