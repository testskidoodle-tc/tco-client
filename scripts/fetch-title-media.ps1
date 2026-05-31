# Downloads YouTube title video + extracts WAV audio for tco client (60fps ffmpeg playback in-game).
# Requires: python + yt-dlp, ffmpeg on PATH (winget install Gyan.FFmpeg)

$ErrorActionPreference = "Stop"
$titleDir = Join-Path $env:APPDATA ".minecraft\meteor-client\title"
New-Item -ItemType Directory -Force -Path $titleDir | Out-Null

$video = Join-Path $titleDir "video.mp4"
$wav = Join-Path $titleDir "title_audio.wav"
$url = "https://www.youtube.com/watch?v=iu_0kOfMGD0"

Write-Host "Downloading video..."
python -m yt_dlp -f "best[height<=720]/best" --merge-output-format mp4 -o $video $url

Write-Host "Extracting audio..."
ffmpeg -y -i $video -vn -acodec pcm_s16le -ar 48000 -ac 2 $wav

Write-Host "Done:"
Write-Host "  $video"
Write-Host "  $wav"
