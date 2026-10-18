#!/bin/sh
# Server self-test (src/selftest SelfTest, max 20 min) on a fresh world: prints the checks and the errors of the run.
cd "$(dirname "$0")/.."
# A crashed run can leave its server (and port 25565) behind: stop the test servers of this project
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match 'selftest' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
rm -rf run/world build-selftest.log
# The watchdog stays on here (tools/stresstest.sh turns it off)
sed -i 's/^max-tick-time=.*/max-tick-time=60000/' run/server.properties
timeout 1200 ./gradlew runSelftest --console=plain "$@" > build-selftest.log 2>&1 < /dev/null
echo "exit $?"
grep -a -E '\[selftest\] (CHECK|DONE)' build-selftest.log | sed -E 's/^.*\[selftest\] //'
echo "--- errors"
grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity' build-selftest.log | grep -v 'NoSuchFileException: server.properties\|\[selftest\] CHECK' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
