---
---
# ChuckLite

Lightweight client-side chunk optimization for Minecraft. Everything runs on your own
game, so nothing needs to be installed on the server, and it plays nicely with
Sodium, Lithium, Iris, and OptiFine.

<div class="chunklite-compare">
  <img
    class="chunklite-img chunklite-base"
    src="https://github.com/1efan/ChuckLite/blob/main/assets/before.png?raw=true"
    alt="Vanilla"
  />

  <img
    class="chunklite-img chunklite-top"
    src="https://github.com/1efan/ChuckLite/blob/main/assets/after.png?raw=true"
    alt="ChunkLite + Compatibles"
  />

  <div class="chunklite-divider">
    <div class="chunklite-handle"></div>
  </div>

  <div class="chunklite-badge chunklite-badge-left">
    <strong>ChunkLite + Compatibles</strong>
    <small>140 FPS</small>
  </div>

  <div class="chunklite-badge chunklite-badge-right">
    <strong>Vanilla</strong>
    <small>92 FPS</small>
  </div>

  <input
    class="chunklite-slider"
    type="range"
    min="0"
    max="100"
    value="50"
    aria-label="Before and after comparison slider"
  />
</div>

<style>
.chunklite-compare {
  --position: 50%;

  position: relative;
  width: 100%;
  max-width: 1920px;
  aspect-ratio: 21 / 9;
  overflow: hidden;
  border-radius: 10px;
  background: #050505;
}

.chunklite-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center center;
  user-select: none;
  pointer-events: none;
}

.chunklite-base {
  z-index: 1;
}

.chunklite-top {
  z-index: 2;
  clip-path: inset(0 calc(100% - var(--position)) 0 0);
  transition: clip-path 0.12s ease-out;
}

.chunklite-divider {
  position: absolute;
  top: 0;
  left: var(--position);
  width: 2px;
  height: 100%;
  z-index: 5;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.9);
  transition: left 0.12s ease-out;
}

.chunklite-handle {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 38px;
  height: 38px;
  transform: translate(-50%, -50%);
  border-radius: 999px;
  background: rgba(20, 20, 20, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.chunklite-handle::before,
.chunklite-handle::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 7px;
  height: 7px;
  border-top: 2px solid #fff;
  border-left: 2px solid #fff;
}

.chunklite-handle::before {
  left: 11px;
  transform: translateY(-50%) rotate(-45deg);
}

.chunklite-handle::after {
  right: 11px;
  transform: translateY(-50%) rotate(135deg);
}

.chunklite-badge {
  position: absolute;
  top: 12px;
  z-index: 6;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 7px 11px;
  color: #fff;
  font-family: Arial, sans-serif;
  line-height: 1.1;
  background: rgba(8, 10, 12, 0.58);
  border-radius: 6px;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.chunklite-badge strong {
  font-size: 14px;
  font-weight: 800;
}

.chunklite-badge small {
  font-size: 12px;
  font-weight: 700;
  opacity: 0.88;
}

.chunklite-badge-left {
  left: 12px;
}

.chunklite-badge-right {
  right: 12px;
  text-align: right;
}

.chunklite-slider {
  position: absolute;
  inset: 0;
  z-index: 10;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: ew-resize;
}
</style>

<script>
(() => {
  const compare = document.querySelector(".chunklite-compare");
  const slider = document.querySelector(".chunklite-slider");

  if (!compare || !slider) return;

  slider.addEventListener("input", () => {
    compare.style.setProperty("--position", `${slider.value}%`);
  });
})();
</script>

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
