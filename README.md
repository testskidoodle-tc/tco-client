# tco client

Java Minecraft utility client based on [Meteor Client](https://github.com/MeteorDevelopment/meteor-client). Rebranded for **tco client** with Discord Rich Presence matching the EarthHack Rewrite / tcohack setup.

## Discord RPC

- **Application ID:** `1510546650597298221`
- **Details:** `tcohack best hack`
- **Large image key:** `subaru`

See [docs/discord-rpc.md](docs/discord-rpc.md) for asset upload steps. Discord Presence is enabled by default on first launch (Misc category).

## Build

```bash
./gradlew build
```

Output JAR: `build/libs/tco-client-<version>.jar`

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) for your Minecraft version.
2. Place the built JAR in your `.minecraft/mods` folder.
3. Launch Minecraft.

## License

This project is licensed under GPL-3.0. Meteor Client is Copyright (c) Meteor Development. See [LICENSE](LICENSE).
