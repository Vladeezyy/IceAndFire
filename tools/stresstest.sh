#!/bin/sh
# Load test (src/selftest StressTest, max 30 min, SEED=<seed> for a given world) on a fresh world: generation, 30 dragons and 100 creatures with AI,
# chunk unload/reload cycles. Prints the tick times, memory, checks and the errors of the run.
cd "$(dirname "$0")/.."
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match 'selftest|stress' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
rm -rf run/world build-stress.log
# Synchronous generation of 16 chunks per tick can exceed the 60 s of the watchdog: the test measures, the watchdog would stop it
sed -i 's/^max-tick-time=.*/max-tick-time=-1/' run/server.properties
grep -q '^max-tick-time' run/server.properties || echo 'max-tick-time=-1' >> run/server.properties
# SEED=<seed> repeats a world (the log prints the seed of every run)
sed -i "s/^level-seed=.*/level-seed=${SEED:-}/" run/server.properties
timeout 1800 ./gradlew runStress --console=plain "$@" > build-stress.log 2>&1 < /dev/null
echo "exit $?"
grep -a '\[stress\]' build-stress.log | sed -E 's/^.*\[stress\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|Can.t keep up' build-stress.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
