# Contributing to ChuckLite

Thanks for your interest in contributing!

## Getting started

1. Fork the repo and clone your fork
2. Set up JDK 17
3. Run `./gradlew build` to verify everything compiles

## Project structure

```
src/main/java/com/chunklite/
├── ChuckLite.java              # Forge @Mod entrypoint
├── ChuckLiteConfig.java        # .properties config (cross-loader)
├── command/
│   └── ChuckLiteCommands.java  # /chunk-lite command
├── mixin/
│   ├── ClientChunkCacheMixin.java   # View-distance clamping
│   └── ChunkStorageAccessor.java    # Chunk array accessor
└── optimizer/
    ├── ChunkLoadThrottle.java       # Per-tick load budget
    └── ClientChunkOptimizer.java    # Core optimization engine

fabric/                         # Standalone Fabric build
└── src/main/java/.../fabric/
    └── ChuckLiteFabric.java    # Fabric entrypoint
```

## Making changes

- Keep the mod client-side — no server dependencies
- Config changes should go through `ChuckLiteConfig`
- Mixin targets are documented with SRG names in the comments
- Test on both Forge and Fabric before opening a PR

## Code style

- 4-space indentation
- Javadoc on public methods
- Follow the existing patterns in the codebase

## Pull requests

1. Create a feature branch from `main`
2. Make your changes
3. Test with `./gradlew runClient`
4. Open a PR with a clear description

Thanks!
