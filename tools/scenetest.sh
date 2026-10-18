#!/bin/sh
# Client scene (src/selftest DevScene, max 15 min): screenshots in run/screenshots/scene_*.png, checks and errors.
cd "$(dirname "$0")/.."
rm -rf run/saves/iaf_scene run/screenshots/scene_*.png build-scene.log
timeout 900 ./gradlew runScene --console=plain "$@" > build-scene.log 2>&1 < /dev/null
echo "exit $?"
grep -a -E '\[scene\] (CHECK|DONE)' build-scene.log | sed -E 's/^.*\[scene\] //'
ls run/screenshots/ 2>/dev/null | grep scene_
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception' build-scene.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
