# ChuckLite

**Lightweight client-side chunk optimization mod for Minecraft**

Reduces memory overhead and improves frame times by intelligently managing chunk load cycles. Plays nicely with Sodium, Embeddium, Rubidium, Lithium, Iris, FerriteCore, Entity Culling, ModernFix, and OptiFine.

## Supported Loaders

| Loader | Versions |
|--------|----------|
| Forge | 1.20.1, 1.21.1 |
| Fabric | 1.20.1, 1.21.1 |

## Features

- **Smart chunk preloading** - prioritizes chunks based on player movement patterns
- **Directional unloading** - keeps chunks in view direction and drops behind first
- **Memory pressure response** - unloads more aggressively when heap usage gets high
- **Render distance clamping** - caps server view distance to configurable bounds
- **Join flood throttling** - spreads chunk processing across ticks when joining
- **`/chunk-lite` command** - stats, force-unload, and config reload in-game

## Commands

```text
/chunk-lite              Show current status and settings
/chunk-lite stats        Dump loaded chunks, heap usage, throttle state
/chunk-lite unload [r]   Force-unload chunks beyond radius r (default 4)
/chunk-lite reload       Reload config from disk
```

## Configuration

Edit `.minecraft/config/chuck-lite.properties`. Changes take effect immediately.

```properties
# Throttling
throttle.enabled=true
throttle.maxPerTick=12

# Directional unloading
directional.enabled=true
directional.retentionAngle=120

# Memory pressure
memory.enabled=true
memory.thresholdPercent=75
memory.aggressiveUnloadCount=8

# Render distance override
renderDistance.enabled=false
renderDistance.min=2
renderDistance.max=16
```

## Compatibility

- Sodium
- Embeddium
- Rubidium
- Lithium
- Iris
- FerriteCore
- Entity Culling
- ModernFix
- OptiFine, with no known conflicts
- Mod Menu on Fabric

ChunkLite is client-side only. Servers do not need to install it.

## Building

### Prerequisites

- Java 17 JDK for Minecraft 1.20.1
- Java 21 JDK for Minecraft 1.21.1
- Git

### Forge 1.20.1

```bash
cd chunklite
./gradlew build
```

### Fabric 1.20.1

```bash
cd chunklite/fabric
./gradlew build
```

### Forge 1.21.1

```bash
cd chunklite/forge-1.21.1
./gradlew build
```

### Fabric 1.21.1

```bash
cd chunklite/fabric-1.21.1
./gradlew build
```

## Installation

Drop the matching JAR into `.minecraft/mods/`.

## Links

- [GitHub](https://github.com/1efan/ChuckLite)
- [Wiki](https://1efan.github.io/ChuckLite/)
- [Issues](https://github.com/1efan/ChuckLite/issues)
- [Discord](https://discord.gg/7DrPzPzKKe)

## Credits

Thanks to the Minecraft optimization community.

## License

MIT - 1efan
