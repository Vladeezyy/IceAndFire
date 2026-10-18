#!/bin/sh
# Riding a dragon on a dedicated server (src/selftest NetTest, issues #5192/#5208): the server runs in run-netserver,
# a client joins it, rides an adult dragon and gets off. Prints the per-second stats of both sides and the errors.
cd "$(dirname "$0")/.."
. tools/common.sh
kill_runs 'nettest|Netserver|Netclient'
rm -rf run-netserver build-netserver.log build-netclient.log
mkdir -p run-netserver
printf 'eula=true\n' > run-netserver/eula.txt
# A typical server: simulation distance 6
printf 'online-mode=false\nserver-port=25570\nsimulation-distance=6\nview-distance=10\nlevel-seed=1234\nspawn-protection=0\nmax-tick-time=-1\n' > run-netserver/server.properties
timed 900 ./gradlew runNetserver --console=plain > build-netserver.log 2>&1 < /dev/null &
i=0
until grep -q -E 'Done \(|Failed to start|Failed to initialize|BUILD FAILED' build-netserver.log 2>/dev/null || [ $i -gt 300 ]; do sleep 2; i=$((i+1)); done
if ! grep -q 'Done (' build-netserver.log; then
  echo "server did not start"
  grep -a -E '/(ERROR|FATAL)\]|Caused by' build-netserver.log | head -10
  exit 1
fi
timed 600 ./gradlew runNetclient --console=plain > build-netclient.log 2>&1 < /dev/null
echo "client exit $?"
wait
for f in build-netserver.log build-netclient.log; do
  echo "=== $f"
  grep -a '\[nettest\]' $f | sed -E 's/^.*\[nettest\] //'
  echo "--- errors"
  grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|moved wrongly|moved too quickly' $f | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -20
done
