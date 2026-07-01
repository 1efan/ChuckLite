# Changelog

All notable changes to ChuckLite will be documented in this file.

## [1.04] - 2026-06-30

### Fixed
- Invalid mod id on Forge. The id was `chunk-lite`, but Forge requires `^[a-z][a-z0-9_]{1,63}$` with no hyphens, so the Forge jar failed to load with "Invalid modId found : chunk-lite". Renamed the Forge id to `chunk_lite` while keeping the public jar name as `chunk-lite`. Thanks to @jacknor7frost for the report (#1).

### Added
- Adaptive frame-budget throttling. Instead of a fixed per-tick cap, ChuckLite now caps chunk GPU uploads per frame to a budget that tracks a rolling average of your frame time against a target FPS: fast frames upload more, slow frames upload fewer, so join floods and fast travel spread across frames instead of spiking one. Nothing is dropped, only deferred.
- Sodium awareness: the throttle detects Sodium (and the Embeddium / Rubidium ports) and steps aside automatically, since Sodium owns the chunk render pipeline.
- New config keys: `throttle.adaptive`, `throttle.targetFps`, `throttle.minPerFrame`, `throttle.maxPerFrame`. The old `throttle.maxPerTick` is now the fixed fallback used only when `throttle.adaptive=false`.
- `/chunk-lite stats` now reports live frame time and the current adaptive budget.
- Adaptive throttling across all supported loaders: Forge and Fabric on 1.20.1 and 1.21.1 (per-frame GPU upload budget), and NeoForge and Fabric on 26.1 (adaptive chunk-batch rate, since 26.1's reworked render pipeline has no per-section upload queue to drain).

### Changed
- Corrected the "per-tick load throttling" wording in the README and docs: the active mechanism is the adaptive frame budget described above.

## [1.02] - 2026-06-22

### Fixed
- Off-by-one chunk coordinate when computing the player's chunk at negative positions, which mis-centered the memory-pressure and force-unload routines

### Added
- Chunk load rate cap on Minecraft 26.1: when throttling is enabled, the client caps its chunk batch size to `throttle.maxPerTick`, smoothing out join spikes without blocking chunk loading

## [1.01] - 2026-06-21

### Fixed
- GC spam: memory-pressure response now enforces a 30-second cooldown between forced collections, preventing repeated full GCs while the heap stays above threshold
- NPE guard: chunk storage accessor now returns null safely during world transitions instead of crashing when `getChunkSource()` returns null
- Directional unload uses the storage view center instead of raw player position, preventing off-by-one misses when the player moves faster than the center updates
- Stats output no longer includes Minecraft formatting codes that render as garbage in the chat log

## [1.00] - 2026-06-21

### Added
- Directional chunk unloading based on player view angle
- Join-flood throttle to eliminate load stutter on server join
- Memory-pressure response with configurable heap threshold
- Render-distance clamping (override server view distance)
- `/chunk-lite` command with `stats`, `unload`, and `reload` subcommands
- `.properties`-based config at `config/chuck-lite.properties`
- Forge 1.20.1+ support (loader 47+)
- Fabric 1.20.1+ support (loader 0.14+)

[1.02]: https://github.com/1efan/ChuckLite/releases/tag/v1.02
[1.01]: https://github.com/1efan/ChuckLite/releases/tag/v1.01
[1.00]: https://github.com/1efan/ChuckLite/releases/tag/v1.00
