#!/bin/sh
# Load test (src/selftest StressTest, max 30 min, SEED=<seed> for a given world) on a fresh world: generation, 30 dragons and 100 creatures with AI,
# chunk unload/reload cycles. Prints the tick times, memory, checks and the errors of the run.
cd "$(dirname "$0")/.."
. tools/common.sh
kill_runs 'selftest|stress'
rm -rf run/world build-stress.log
# Synchronous generation of 16 chunks per tick can exceed the 60 s of the watchdog: the test measures, the watchdog would stop it
set_prop max-tick-time -1
# SEED=<seed> repeats a world (the log prints the seed of every run)
set_prop level-seed "${SEED:-}"
timed 1800 ./gradlew runStress --console=plain "$@" > build-stress.log 2>&1 < /dev/null
echo "exit $?"
grep -a '\[stress\]' build-stress.log | sed -E 's/^.*\[stress\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|Can.t keep up' build-stress.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
