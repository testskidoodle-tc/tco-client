# tco client

Java Minecraft utility client based on [Meteor Client](https://github.com/MeteorDevelopment/meteor-client).

## Features

- **Rebrand** — Shows as **tco client** in mod list, chat prefix `[tco]`, HUD group, splashes
- **Discord RPC** — App `1510546650597298221`, details `tcohack best hack`, image `subaru` (see [docs/discord-rpc.md](docs/discord-rpc.md))
- **ClickGUI** — Compact RusherHack-style dark theme (Tco theme: smaller scale, left-aligned modules, blue accent)
- **Windows XP boot** — XP-style progress splash before the title screen (first launch per session)
- **Title music** — Bundled track from [YouTube](https://www.youtube.com/watch?v=iu_0kOfMGD0); re-fetch with `scripts/fetch-title-music.ps1`

## Build

```bash
./gradlew build
```

Output: `build/libs/tco-client-<version>.jar`

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **26.1.2** (match `gradle/libs.versions.toml`).
2. Copy the JAR into `.minecraft/mods`.
3. Upload `docs/discord-assets/subaru.png` to your Discord app as asset **`subaru`** for RPC artwork.

## License

GPL-3.0 — Meteor Client Copyright (c) Meteor Development. See [LICENSE](LICENSE).
