package com.github.jikoo.planarwrappers.util.version;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public final class BukkitVersions {

  /**
   * The Minecraft version.
   */
  public static final Version MINECRAFT;
  /**
   * The {@link Version} of Craftbukkit's package. Do not confuse with {@link #MINECRAFT}!
   *
   * <p>The major and minor version correspond to Minecraft's major and minor version. The patch
   * version is an internal version that is intended to be bumped whenever the Minecraft mappings
   * version ({@code org.bukkit.craftbukkit.util.CraftMagicNumbers#getMappingsVersion}) changes.
   *
   * <p>Note that if you are writing a plugin using NMS, you should prefer checking against the
   * mappings version. Spigot has been very unreliable about bumping the minor version, claiming
   * that the mappings version is the ultimate way to check if your NMS usage is guaranteed to be
   * compatible. This is true, but only because they're unreliable about bumping it. If they
   * actually kept up with bumping it (which could have been scripted) it would be just as reliable
   * and much more human-friendly.
   * @deprecated Prefer {@code org.bukkit.craftbukkit.util.CraftMagicNumbers#getMappingsVersion}
   */
  @Deprecated
  public static final Version CRAFTBUKKIT_PACKAGE;

  static {
    Server server = Bukkit.getServer();
    MINECRAFT = parseMinecraftVersion(server);

    String packageString = server.getClass().getPackage().toString();
    CRAFTBUKKIT_PACKAGE = parseCraftbukkitVersion(packageString);
  }

  @VisibleForTesting
  @Contract("_ -> new")
  static @NotNull Version parseMinecraftVersion(Server server) {
    Pattern semVerRelease = Pattern.compile("^(\\d+)\\.(\\d+)(\\.(\\d+))?-.*$");

    String versionString;
    try {
      // Paper adds Server#getMinecraftVersion
      MethodHandle getMcVer = MethodHandles.publicLookup()
          .findVirtual(Server.class, "getMinecraftVersion", MethodType.methodType(String.class));
      versionString = (String) getMcVer.invoke(server);

    } catch (Throwable e) {
      // Server is not implementing Paper's API.
      // Note: we use Bukkit version and not server version because server version includes prefixes
      // (such as implementation name and commit hashes of build version) that are not presented in
      // valid SemVer format.
      // Bukkit version should be a (mostly) SemVer-compliant version including Minecraft's version.
      versionString = Bukkit.getBukkitVersion();
    }

    Matcher matcher = semVerRelease.matcher(versionString);

    if (matcher.find()) {
      return Version.of(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(4))
      );
    }

    // If Bukkit version has been modified (oh boy) fall through to using whatever Version selects
    // with extra SemVer details stripped. Spigot hasn't ever cut a Bukkit release, so
    // the pre-release data only will serve to cause comparison confusion.
    return Version.of(stripExtraData(versionString));
  }

  @VisibleForTesting
  static @NotNull Version parseCraftbukkitVersion(@NotNull String craftbukkitPackage) {
    Pattern packageVer = Pattern.compile("^org\\.bukkit\\.craftbukkit\\.v(\\d+)_(\\d+)_R?(\\d+)");
    Matcher matcher = packageVer.matcher(craftbukkitPackage);

    if (matcher.find()) {
      return Version.of(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3))
      );
    }

    // Ancient/unknown version; package is not versioned. Best bet is to use the Minecraft version.
    return MINECRAFT;
  }

  private static @NotNull String stripExtraData(@NotNull String version) {
    int preReleaseStart = version.indexOf('-');
    int metadataStart = version.indexOf('+');
    if (preReleaseStart > -1) {
      if (metadataStart > -1) {
        return version.substring(0, Math.min(preReleaseStart, metadataStart));
      } else {
        return version.substring(0, preReleaseStart);
      }
    } else {
      if (metadataStart > -1) {
        return version.substring(0, metadataStart);
      }
    }
    return version;
  }

  private BukkitVersions() {
    throw new IllegalStateException("Cannot instantiate static utility classes!");
  }

}
