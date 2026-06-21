# Changelog

All notable changes to ChuckLite will be documented in this file.

## [1.2.1] -- 2024-06-21

### Fixed
- GC spam: memory-pressure response now enforces a 30-second cooldown between forced collections, preventing repeated full GCs while the heap stays above threshold
- NPE guard: chunk storage accessor now returns null safely during world transitions instead of crashing when `getChunkSource()` returns null
- Directional unload uses the storage view center instead of raw player position, preventing off-by-one misses when the player moves faster than the center updates
- Stats output no longer includes Minecraft formatting codes that render as garbage in the chat log

## [1.2.0] -- 2024-06-21

### Added
- Directional chunk unloading based on player view angle
- Join-flood throttle to eliminate load stutter on server join
- Memory-pressure response with configurable heap threshold
- Render-distance clamping (override server view distance)
- `/chunk-lite` command with `stats`, `unload`, and `reload` subcommands
- `.properties`-based config at `config/chuck-lite.properties`
- Forge 1.20.1+ support (loader 47+)
- Fabric 1.20.1+ support (loader 0.14+)

[1.2.1]: https://github.com/1efan/ChuckLite/releases/tag/v1.2.1
[1.2.0]: https://github.com/1efan/ChuckLite/releases/tag/v1.2.0
