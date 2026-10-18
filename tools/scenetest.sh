#!/bin/sh
# Client scene (src/selftest DevScene, max 15 min): screenshots in run/screenshots/scene_*.png, checks and errors.
cd "$(dirname "$0")/.."
# A crashed run can leave its client behind: stop the scene clients of this project
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match 'runScene|iceandfire.scene' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
rm -rf run/saves/iaf_scene run/screenshots/scene_*.png build-scene.log
timeout 900 ./gradlew runScene --console=plain "$@" > build-scene.log 2>&1 < /dev/null
echo "exit $?"
grep -a -E '\[scene\] (CHECK|DONE)' build-scene.log | sed -E 's/^.*\[scene\] //'
ls run/screenshots/ 2>/dev/null | grep scene_
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity' build-scene.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
