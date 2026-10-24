#!/bin/sh
# Server self-test (src/selftest SelfTest, max 20 min) on a fresh world: prints the checks and the errors of the run.
cd "$(dirname "$0")/.."
# A crashed run can leave its server (and port 25565) behind: stop the test servers of this project
. tools/common.sh
kill_runs 'selftest'
rm -rf $RUN/world build-selftest.log
# The watchdog stays on here (tools/stresstest.sh turns it off)
set_prop max-tick-time 60000
timed 1200 ./gradlew :$LOADER:runSelftest --console=plain "$@" > build-selftest.log 2>&1 < /dev/null
echo "exit $?"
grep -a -E '\[selftest\] (CHECK|DONE)' build-selftest.log | sed -E 's/^.*\[selftest\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity' build-selftest.log | grep -v 'NoSuchFileException: server.properties\|\[selftest\] CHECK' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
