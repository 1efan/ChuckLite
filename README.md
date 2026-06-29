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

ChuckLite fixes two things that make chunks lag your client. One is the spike of
work when a lot of chunks show up at once. The other is the memory those chunks
keep using after they leave your screen. It handles both, and it only runs on your
own game.

### How it helps your frame rate

**Chunk loading gets spread out.** When you join a server, teleport, or move fast
on an elytra or a horse, the server sends a big batch of chunks and the normal
client tries to build all of them in the same tick. That is the stutter you feel
on join and the freeze when you fast travel. ChuckLite limits how many chunks get
built each tick (12 by default) and puts the rest in a queue, then works through
that queue a few at a time. You still get every chunk, it just does not all land in
one frame.

**Chunks behind you get dropped first.** You are not looking at the chunks behind
your head, but the normal client keeps them loaded anyway. Every few ticks ChuckLite
unloads the far ring of chunks that sit outside the cone you are facing (120 degrees
by default). The chunks in front of you stay, the ones behind you go, and both your
memory and your frame rate get some breathing room.

**It backs off when memory gets tight.** Four times a second it checks how full the
Java heap is. Once it passes the limit you set (75% by default) it starts unloading
the farthest chunks, keeps the closer half of your view, and runs a garbage
collection that will not fire again right away. If you do not have much RAM, this is
the part that stops the long-session freezes and the out of memory crashes.

You can also cap your render distance to a range if your hardware needs it. The
settings live in a config file that reloads the moment you save it, and since none
of this touches the server, it works on vanilla, Hypixel, Realms, or wherever you
play.

## Features

| Feature | What it does |
|---------|--------------|
| Per-tick load throttling | Builds a set number of chunks each tick and queues the rest, so join floods and fast travel spread out instead of stuttering in one frame |
| Directional unloading | Unloads the far ring of chunks behind you first and keeps the ones you are facing, so memory frees up without changing what you see |
| Memory-pressure response | Watches the Java heap and unloads the farthest chunks once it gets too full, keeps the closer half of your view, then runs a spaced-out GC |
| Render-distance clamping | Optional cap that holds your view distance inside a min and max range for weaker hardware |
| In-game monitoring | `/chunk-lite stats` shows loaded chunks, heap usage, and the queue |
| Zero server impact | Everything runs on your client, so it works on vanilla servers, Hypixel, Realms, and anywhere else |

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
