<p align="center">
  <img src="src/main/resources/assets/chunk-lite/icon.png" width="128" alt="ChuckLite">
</p>

<h1 align="center">ChuckLite</h1>

<p align="center">
  <strong>Lightweight client-side chunk optimization for Minecraft</strong>
</p>

<p align="center">
  <a href="https://github.com/1efan/ChuckLite/releases"><img src="https://img.shields.io/github/v/release/1efan/ChuckLite?style=flat-square&color=blue" alt="Release"></a>
  <a href="https://github.com/1efan/ChuckLite/blob/main/LICENSE"><img src="https://img.shields.io/github/license/1efan/ChuckLite?style=flat-square&color=green" alt="License"></a>
  <a href="https://github.com/1efan/ChuckLite/issues"><img src="https://img.shields.io/github/issues/1efan/ChuckLite?style=flat-square" alt="Issues"></a>
  <br>
  <img src="https://img.shields.io/badge/Minecraft-1.20.x-62b543?style=flat-square" alt="Minecraft">
  <img src="https://img.shields.io/badge/Forge-47+-orange?style=flat-square" alt="Forge">
  <img src="https://img.shields.io/badge/Fabric-0.14+-d5d5d5?style=flat-square" alt="Fabric">
  <img src="https://img.shields.io/badge/Side-Client_Only-blue?style=flat-square" alt="Side">
</p>

---

Reduces memory overhead and improves frame times by intelligently managing
chunk load cycles. Everything runs client-side, so nothing needs to be installed
on a server.

## Features

| Feature | Description |
|---------|-------------|
| Smart directional unloading | Keeps chunks in your view direction longer, drops chunks behind you first |
| Join-flood throttling | Spreads chunk processing across ticks when joining a server, eliminating load stutter |
| Memory-pressure response | Aggressively unloads distant chunks when heap exceeds the configured threshold |
| Render-distance clamping | Caps the server's view distance to protect weaker hardware |
| In-game monitoring | `/chunk-lite stats` shows loaded chunks, heap usage, and throttle state |
| Zero server impact | All optimizations are strictly client-side, so it works on vanilla, Hypixel, and anywhere else |

## Compatibility

| Mod | Status |
|-----|--------|
| Sodium | Compatible |
| Lithium | Compatible |
| OptiFine | No known conflicts |
| Iris / Oculus | Compatible |
| Mod Menu (Fabric) | Supported |

## Installation

1. Install Forge (47+) or Fabric (0.14+) for Minecraft 1.20.x
2. Download `chunk-lite-1.2.0.jar` from [Releases](https://github.com/1efan/ChuckLite/releases)
3. Drop it into `.minecraft/mods/`
4. Launch the game. ChuckLite works right away with sensible defaults

No server install required.

## Configuration

Edit `.minecraft/config/chuck-lite.properties`. Changes take effect immediately.

```properties
# Throttling: cap chunk processing during join
throttle.enabled=true
throttle.maxPerTick=12

# Directional unloading: keep what you look at
directional.enabled=true
directional.retentionAngle=120

# Memory pressure: aggressive unload when heap is full
memory.enabled=true
memory.thresholdPercent=75
memory.aggressiveUnloadCount=8

# Render distance: clamp server view distance
renderDistance.enabled=false
renderDistance.min=2
renderDistance.max=16
```

## Commands

| Command | Description |
|---------|-------------|
| `/chunk-lite` | Show current status and settings |
| `/chunk-lite stats` | Dump loaded chunks, heap usage, throttle queue |
| `/chunk-lite unload [radius]` | Force-unload chunks outside radius (default 4) |
| `/chunk-lite reload` | Reload config from disk |

## Building

Prerequisites: Java 17 JDK

### Forge
```bash
git clone https://github.com/1efan/ChuckLite.git
cd ChuckLite
./gradlew build      # outputs build/libs/chunk-lite-1.2.0.jar
./gradlew runClient  # launch in dev
```

### Fabric
```bash
cd ChuckLite/fabric
./gradlew build      # outputs fabric/build/libs/chunk-lite-1.2.0.jar
./gradlew runClient
```

## License

Licensed under the MIT License. Copyright (c) 1efan.

---

<p align="center">
  <sub>Thanks to the Minecraft optimization community.</sub>
</p>
