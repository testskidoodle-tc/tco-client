# tco client

Java Minecraft utility client based on [Meteor Client](https://github.com/MeteorDevelopment/meteor-client).

## Features

- **Rebrand** — tco client / tcohack RPC / custom splashes
- **Discord RPC** — App `1510546650597298221`, `tcohack best hack`, `subaru` image
- **XP boot splash** — procedural **tco OS xp** screen
- **Title screen video** — full **60fps** playback from YouTube via **ffmpeg** (not PNG slideshow)
- **Title audio** — extracted from the same video, synced loop

## Title media (YouTube)

Source: https://www.youtube.com/watch?v=iu_0kOfMGD0

On first title screen visit the mod downloads to:

`%APPDATA%\.minecraft\meteor-client\title\video.mp4`  
`%APPDATA%\.minecraft\meteor-client\title\title_audio.wav`

**Requirements:** `ffmpeg` on PATH (`winget install Gyan.FFmpeg`) and `python -m pip install yt-dlp`

Or run manually:

```powershell
.\scripts\fetch-title-media.ps1
```

Minecraft cannot embed YouTube directly — ffmpeg decodes real MP4 frames to the GPU at 60fps.

## Build

```bash
./gradlew build
```

## Install

Minecraft **26.1.2** + Fabric Loader → place JAR in `.minecraft/mods`

## License

GPL-3.0 — Meteor Client Copyright (c) Meteor Development.
