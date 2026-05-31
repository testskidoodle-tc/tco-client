# Downloads title screen music from YouTube and converts to OGG for Minecraft.
# Requires: Python (pip install yt-dlp), ffmpeg on PATH

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "src\main\resources\assets\meteor-client\sounds\tco"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$url = "https://www.youtube.com/watch?v=iu_0kOfMGD0"
$webm = Join-Path $outDir "title_theme.webm"
$ogg = Join-Path $outDir "title_theme.ogg"

python -m yt_dlp -f bestaudio -o $webm $url
ffmpeg -y -i $webm -c:a libvorbis -q:a 4 $ogg
Remove-Item $webm -ErrorAction SilentlyContinue
Write-Host "Wrote $ogg"
