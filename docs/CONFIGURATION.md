---
---
# Configuration and commands

ChuckLite reads its settings from `.minecraft/config/chuck-lite.properties`. The file
is written with sensible defaults the first time you launch, and any change you make
takes effect as soon as you save it. There is nothing to restart.

```properties
# Throttling: spread chunk building across ticks
throttle.enabled=true
throttle.maxPerTick=12

# Directional unloading: keep what you look at, drop what is behind you
directional.enabled=true
directional.retentionAngle=120

# Memory pressure: unload distant chunks when the heap fills up
memory.enabled=true
memory.thresholdPercent=75
memory.aggressiveUnloadCount=8

# Render distance: clamp the effective view distance
renderDistance.enabled=false
renderDistance.min=2
renderDistance.max=16
```

## What each setting does

| Setting | Default | Range | Meaning |
|---------|---------|-------|---------|
| `throttle.enabled` | true | on/off | Turn the per-tick load limit on or off |
| `throttle.maxPerTick` | 12 | 1 to 50 | How many chunks may be built in a single tick before the rest wait |
| `directional.enabled` | true | on/off | Turn directional unloading on or off |
| `directional.retentionAngle` | 120 | 60 to 180 | Width of the forward cone whose chunks are kept. Smaller drops more behind you |
| `memory.enabled` | true | on/off | Turn the memory-pressure response on or off |
| `memory.thresholdPercent` | 75 | 50 to 95 | Heap usage that triggers aggressive unloading |
| `memory.aggressiveUnloadCount` | 8 | 1 to 30 | How many far chunks to drop per pass once over the threshold |
| `renderDistance.enabled` | false | on/off | Turn the render-distance clamp on or off |
| `renderDistance.min` | 2 | 2 to 32 | Lowest view distance the clamp allows |
| `renderDistance.max` | 16 | 4 to 64 | Highest view distance the clamp allows |

## Commands

| Command | What it does |
|---------|--------------|
| `/chunk-lite` | Show the current status and settings |
| `/chunk-lite stats` | Dump loaded chunks, heap usage, and the throttle queue |
| `/chunk-lite unload [radius]` | Force-unload chunks outside the given radius (default 4) |
| `/chunk-lite reload` | Reload the config from disk |
