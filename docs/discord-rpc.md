# Discord Rich Presence setup

tco client uses Discord RPC via the **discord-presence** module (Misc category), enabled by default.

## Default presence

- **Application ID:** `1510546650597298221`
- **Details:** `tcohack best hack`
- **Large image key:** `subaru` (Subaru Natsuki from Re:Zero)

## Upload the Subaru image

1. Open your Discord application at https://discord.com/developers/applications
2. Go to **Rich Presence → Art Assets**
3. Upload `docs/discord-assets/subaru.png` with asset name **`subaru`**

Discord only loads images from the developer portal — the PNG in this repo is for reference when uploading.

## Enable in game

1. Open ClickGui (**Right Shift**)
2. Misc → **discord-presence** (on by default)
3. Edit line messages in module settings if you want custom details/state text
