#!/bin/sh
# Load test (src/selftest StressTest, max 30 min) on a fresh world: generation, 30 dragons and 100 creatures with AI,
# chunk unload/reload cycles. Prints the tick times, memory, checks and the errors of the run.
cd "$(dirname "$0")/.."
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match 'selftest|stress' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
rm -rf run/world build-stress.log
timeout 1800 ./gradlew runStress --console=plain "$@" > build-stress.log 2>&1 < /dev/null
echo "exit $?"
grep -a '\[stress\]' build-stress.log | sed -E 's/^.*\[stress\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|Can.t keep up' build-stress.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
