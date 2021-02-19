# PlanarWrappers

![Build](https://github.com/Jikoo/PlanarWrappers/workflows/Build/badge.svg)
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

### Functions

I keep rewriting the same functional interfaces. It may take five seconds, but it's five seconds
each and every time that I could have spent doing anything else.

## For Developers

### Maven

PlanarWrappers is available via [JitPack](https://jitpack.io). Replace `$planarVersion` with the
version you desire to work with. Please relocate PlanarWrappers when shading it into your file!
Bundled library conflicts are not fun, make your life easier. The `minimizeJar` option is
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
      <version>$planarVersion</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.2.4</version>
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
