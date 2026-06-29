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

ChuckLite targets the two things that cause chunk-related lag on the client: the
burst of work when many chunks arrive at once, and the memory those chunks keep
holding after they leave your screen. It fixes both with three mechanisms, all
running only on your game.

### How it improves performance

**Chunk-load throttling.** When you join a server, teleport, or move fast (elytra,
boat, horse, `/tp`), the server streams a flood of chunks and the vanilla client
tries to build them all in the same tick. That spike in a single frame is the
classic "join stutter" and the freeze you get when fast-travelling. ChuckLite caps
how many chunk builds run per client tick (12 by default) and queues the overflow,
draining a few each tick so frame time stays flat instead of hitching. Nothing is
discarded, the work is just spread across a handful of ticks.

**Directional unloading.** Chunks behind your camera are not being rendered, but
the vanilla client keeps them fully resident anyway. A few times a second ChuckLite
drops the outer ring of chunks that fall outside a forward-facing cone (120 degrees
by default), so the chunks you are actually looking at stay loaded while the ones
behind you free up. Fewer resident chunks means lower memory use and less per-tick
bookkeeping, which shows up as a higher, steadier frame rate.

**Memory-pressure response.** Four times a second it reads JVM heap usage. If you
cross a threshold (75% by default) it unloads the farthest chunks first, always
keeping the inner half of your view radius intact, and nudges a rate-limited
garbage collection. On lower-RAM machines this is what prevents the GC-thrash
freezes and out-of-memory crashes that long play sessions otherwise build toward.

Optionally it can clamp the effective render distance to a min/max range to protect
weaker hardware. Everything is configurable and hot-reloads from disk, and because
it is 100% client-side it works on vanilla servers, Hypixel, Realms, anywhere, with
nothing installed server-side.

## Features

| Feature | What it does |
|---------|--------------|
| Per-tick load throttling | Caps chunk builds per tick and queues the rest, so join floods and fast-travel are spread across ticks instead of stuttering in one frame |
| Directional unloading | Drops the outer ring of chunks outside your forward view cone first, keeping what you look at loaded and freeing memory behind you |
| Memory-pressure response | Watches JVM heap and unloads the farthest chunks (inner radius preserved) once usage crosses the threshold, then triggers a rate-limited GC |
| Render-distance clamping | Optionally bounds the effective view distance to a min/max range to protect weaker hardware |
| In-game monitoring | `/chunk-lite stats` shows loaded chunks, heap usage, and the throttle queue |
| Zero server impact | All optimizations are strictly client-side, so it works on vanilla, Hypixel, Realms, and anywhere else |

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
2. Download `chunk-lite-1.01.jar` from [Releases](https://github.com/1efan/ChuckLite/releases)
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
./gradlew build      # outputs build/libs/chunk-lite-1.01.jar
./gradlew runClient  # launch in dev
```

### Fabric
```bash
cd ChuckLite/fabric
./gradlew build      # outputs fabric/build/libs/chunk-lite-1.01.jar
./gradlew runClient
```

## License

Licensed under the MIT License. Copyright (c) 1efan.

---

<p align="center">
  <sub>Thanks to the Minecraft optimization community.</sub>
</p>
