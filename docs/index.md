---
---
# ChuckLite

Lightweight client-side chunk optimization for Minecraft. Everything runs on your own
game, so nothing needs to be installed on the server, and it plays nicely with
Sodium, Lithium, Iris, and OptiFine.

## How it helps your frame rate

**Chunk loading gets spread out.** When you join a server, teleport, or move fast on
an elytra or a horse, the server sends a big batch of chunks and the normal client
tries to build all of them in the same tick. That is the stutter you feel on join
and the freeze when you fast travel. ChuckLite limits how many chunks get built each
tick (12 by default) and puts the rest in a queue, then works through that queue a
few at a time. You still get every chunk. It just does not all land in one frame.

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
| Per-tick load throttling | Builds a set number of chunks each tick and queues the rest, so join floods and fast travel spread out instead of stuttering in one frame |
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
