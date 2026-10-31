#!/bin/sh
# Riding a dragon on a dedicated server (src/selftest NetTest, issues #5192/#5208): the server runs in run-netserver,
# a client joins it, rides an adult dragon and gets off. Prints the per-second stats of both sides and the errors.
cd "$(dirname "$0")/.."
. tools/common.sh
kill_runs 'nettest|Netserver|Netclient'
rm -rf $LOADER/run-netserver build-netserver.log build-netclient.log
mkdir -p $LOADER/run-netserver
printf 'eula=true\n' > $LOADER/run-netserver/eula.txt
# A typical server: simulation distance 6
# 26.3 servers turn the white list on by default; 26.x pauses an empty server after 60 s
printf 'online-mode=false\nserver-port=25570\nsimulation-distance=6\nview-distance=10\nlevel-seed=1234\nspawn-protection=0\nmax-tick-time=-1\nwhite-list=false\nenforce-whitelist=false\npause-when-empty-seconds=0\n' > $LOADER/run-netserver/server.properties
timed 900 ./gradlew :$LOADER:runNetserver --console=plain > build-netserver.log 2>&1 < /dev/null &
wait_started build-netserver.log 'Failed to start|Failed to initialize'
if ! grep -q 'Done (' build-netserver.log; then
  echo "server did not start"
  grep -a -E '/(ERROR|FATAL)\]|Caused by' build-netserver.log | head -10
  exit 1
fi
run_watched build-netclient.log 600 ./gradlew :$LOADER:runNetclient --console=plain < /dev/null
wait
for f in build-netserver.log build-netclient.log; do
  echo "=== $f"
  grep -a '\[nettest\]' $f | sed -E 's/^.*\[nettest\] //'
  echo "--- errors"
  grep -a -E '/(ERROR|FATAL)\]|Caused by|Exception|UUID of added entity|moved wrongly|moved too quickly' $f | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -20
done
