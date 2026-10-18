#!/bin/sh
# Riding a dragon on a dedicated server (src/selftest NetTest, issues #5192/#5208): the server runs in run-netserver,
# a client joins it, rides an adult dragon and gets off. Prints the per-second stats of both sides and the errors.
cd "$(dirname "$0")/.."
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match 'nettest|Netserver|Netclient' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
rm -rf run-netserver build-netserver.log build-netclient.log
mkdir -p run-netserver
printf 'eula=true\n' > run-netserver/eula.txt
# A typical server: simulation distance 6
printf 'online-mode=false\nserver-port=25570\nsimulation-distance=6\nview-distance=10\nlevel-seed=1234\nspawn-protection=0\nmax-tick-time=-1\n' > run-netserver/server.properties
timeout 900 ./gradlew runNetserver --console=plain > build-netserver.log 2>&1 < /dev/null &
i=0
until grep -q 'Done (' build-netserver.log 2>/dev/null || [ $i -gt 300 ]; do sleep 2; i=$((i+1)); done
timeout 600 ./gradlew runNetclient --console=plain > build-netclient.log 2>&1 < /dev/null
echo "client exit $?"
wait
for f in build-netserver.log build-netclient.log; do
  echo "=== $f"
  grep -a '\[nettest\]' $f | sed -E 's/^.*\[nettest\] //'
  echo "--- errors"
  grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|moved wrongly|moved too quickly' $f | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -20
done
