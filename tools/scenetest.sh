#!/bin/sh
# Client scene (src/selftest DevScene, max 15 min): screenshots in run/screenshots/scene_*.png, checks and errors.
cd "$(dirname "$0")/.."
# A crashed run can leave its client behind: stop the scene clients of this project
. tools/common.sh
kill_runs 'runScene|iceandfire.scene'
rm -rf $RUN/saves/iaf_scene $RUN/screenshots/scene_*.png build-scene.log
# SCENE_LANG=ru_ru (or uk_ua...): the language of the client for this run, English again afterwards
lang_option() {
  if grep -q '^lang:' $RUN/options.txt; then perl -pi -e "s/^lang:.*/lang:$1/" $RUN/options.txt; else echo "lang:$1" >> $RUN/options.txt; fi
}
lang_option "${SCENE_LANG:-en_us}"
timed 900 ./gradlew :$LOADER:runScene --console=plain "$@" > build-scene.log 2>&1 < /dev/null
status=$?
lang_option en_us
echo "exit $status"
grep -a -E '\[scene\] (CHECK|DONE)' build-scene.log | sed -E 's/^.*\[scene\] //'
ls $RUN/screenshots/ 2>/dev/null | grep scene_
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity' build-scene.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
