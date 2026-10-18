#!/bin/sh
# Server self-test (src/selftest SelfTest, max 20 min) on a fresh world: prints the checks and the errors of the run.
cd "$(dirname "$0")/.."
rm -rf run/world build-selftest.log
timeout 1200 ./gradlew runSelftest --console=plain "$@" > build-selftest.log 2>&1 < /dev/null
echo "exit $?"
grep -a -E '\[selftest\] (CHECK|DONE)' build-selftest.log | sed -E 's/^.*\[selftest\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception' build-selftest.log | grep -v 'NoSuchFileException: server.properties\|\[selftest\] CHECK' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
