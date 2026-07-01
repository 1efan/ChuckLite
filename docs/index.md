---
---
# ChuckLite

Lightweight client-side chunk optimization for Minecraft. Everything runs on your own
game, so nothing needs to be installed on the server, and it plays nicely with
Sodium, Lithium, Iris, and OptiFine.

## How it helps your frame rate

**Chunk uploads get spread out, and the amount adapts to your frame rate.** When you
join a server, teleport, or move fast on an elytra or a horse, a flood of chunk meshes
finishes at once and the normal client uploads all of them to the GPU on a single
frame. That is the stutter you feel on join and the freeze when you fast travel.
ChuckLite caps how many uploads happen per frame and lets the rest wait for the next
frames. The cap is not a fixed number: it tracks a rolling average of your frame time
against a target FPS, uploading more when frames run fast and fewer when they dip.
Nothing is dropped, it just spreads across frames. When Sodium (or Embeddium /
Rubidium) is installed it owns the chunk render pipeline, so ChuckLite steps aside.

**Chunks behind you get dropped first.** You are not looking at the chunks behind
your head, but the normal client keeps them loaded anyway. Every few ticks ChuckLite
unloads the far ring of chunks that sit outside the cone you are facing (120 degrees
by default). The chunks in front of you stay while the ones behind you go, which
gives both your memory and your frame rate some breathing room.

**It backs off when memory gets tight.** Four times a second it checks how full the
Java heap is. Once it passes the limit you set (75% by default), it unloads the
farthest chunks, keeps the closer half of your view, and runs a garbage collection
that will not fire again right away. If you do not have much RAM, this is the part
that stops the long-session freezes and the out-of-memory crashes.

You can also cap your render distance to a range if your hardware needs it.

## Features

| Feature | What it does |
|---------|--------------|
| Adaptive frame-budget throttling | Caps chunk GPU uploads per frame to a budget that tracks your frame time and target FPS, so join floods and fast travel spread across frames instead of spiking one. Steps aside when Sodium owns the render path |
| Directional unloading | Unloads the far ring of chunks behind you first and keeps the ones you are facing, so memory frees up without changing what you see |
| Memory-pressure response | Watches the Java heap and unloads the farthest chunks once it gets too full, keeps the closer half of your view, then runs a spaced-out GC |
| Render-distance clamping | Optional cap that holds your view distance inside a min and max range for weaker hardware |
| In-game monitoring | `/chunk-lite stats` shows loaded chunks, heap usage, and the queue |
| Zero server impact | Everything runs on your client, so it works on vanilla servers, Hypixel, Realms, and anywhere else |

## Settings and commands

See the [configuration reference](CONFIGURATION.md) for the config file, what each
setting does, and the in-game commands.

## Get the mod

- [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/chunklite)
- [Source on GitHub](https://github.com/1efan/ChuckLite)

Works on Minecraft 1.20.x with Forge (47+) or Fabric (0.14+). See the
[README](https://github.com/1efan/ChuckLite#readme) for install steps.
