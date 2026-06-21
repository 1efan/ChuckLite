# ChuckLite v1.2.0

**Lightweight client-side chunk optimization mod for Minecraft**

Reduces memory overhead and improves frame times by intelligently managing
chunk load cycles. Compatible with Sodium, Lithium, and other optimization mods.

## Supported Loaders

| Loader  | Versions                |
|---------|-------------------------|
| Forge   | 1.20.1–1.20.x (loader 47+)    |
| Fabric  | 1.20.1+ (loader 0.14+)         |

## Features

- **Smart chunk preloading** — prioritizes chunks based on player movement patterns
- **Directional unloading** — keeps chunks in view direction, drops behind first
- **Memory-pressure response** — aggressively unloads when heap exceeds threshold
- **Render-distance clamping** — caps server view distance to configurable bounds
- **Join-flood throttling** — spreads chunk processing across ticks when joining
- **`/chunk-lite` command** — stats, force-unload, config reload in-game

## Commands

```
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

## Building

### Prerequisites
- Java 17 JDK
- Git

### Forge

```bash
cd chunklite
gradle wrapper          # first time only
./gradlew build         # → build/libs/chunk-lite-1.2.0.jar
./gradlew runClient     # launch in dev
```

### Fabric

```bash
cd chunklite/fabric
gradle wrapper          # first time only  
./gradlew build         # → fabric/build/libs/chunk-lite-1.2.0.jar
./gradlew runClient     # launch in dev
```

## Installation

Drop the JAR into `.minecraft/mods/`. No server install needed — all
optimizations are strictly client-side.

## Compatibility

- ✅ Sodium
- ✅ Lithium
- ✅ OptiFine (no known conflicts)
- ✅ Mod Menu (Fabric)

## Links

- [GitHub](https://github.com/1efan/ChuckLite)
- [Issues](https://github.com/1efan/ChuckLite/issues)

## Credits

Thanks to the Minecraft optimization community.

## License

MIT — © 1efan
