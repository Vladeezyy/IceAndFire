# Shared helpers of the test scripts, the same on Windows (Git Bash) and macOS/Linux.
#   kill_runs <regex>        stop java processes of this project whose command line matches <regex> (not the Gradle daemon)
#   timed <seconds> cmd...   run cmd with a time limit (no GNU timeout on macOS)
#   set_prop <key> <value>   set a key in $RUN/server.properties (creates the file)
# LOADER=neoforge (default) or fabric: its run directory and Gradle runs
LOADER="${LOADER:-neoforge}"
# The hang watchdog of the knowledge base (testing/hang-watchdog.md): run_watched for clients and servers that stop by
# themselves, wait_started for a server in the background; kill_dev stops this project's game processes on a hang
. tools/watchdog.sh
kill_dev() { kill_runs '.'; }
RUN="$LOADER/run"

kill_runs() {
  if command -v powershell >/dev/null 2>&1; then
    powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'IceFire' -and \$_.CommandLine -match '$1' -and \$_.CommandLine -notmatch 'GradleDaemon' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null
  else
    # only java processes: the shell running this script has the same words in its command line
    ps -axo pid=,comm=,command= | awk '$2 ~ /java$/' | grep -i -E 'icefire|iceandfire' | grep -E "$1" | grep -v GradleDaemon | awk '{print $1}' | xargs kill -9 2>/dev/null
  fi
  return 0
}
timed() {
  secs=$1; shift
  perl -e 'alarm shift; exec @ARGV' "$secs" "$@"
}
set_prop() {
  mkdir -p "$RUN"
  touch $RUN/server.properties
  if grep -q "^$1=" $RUN/server.properties; then
    perl -pi -e "s/^\Q$1\E=.*/$1=$2/" $RUN/server.properties
  else
    echo "$1=$2" >> $RUN/server.properties
  fi
}

# A fresh checkout has no $RUN/ (it is ignored): the test server needs the EULA (accepted by the owner) and a test setup
mkdir -p "$RUN"
[ -f $RUN/eula.txt ] || echo 'eula=true' > $RUN/eula.txt
[ -f $RUN/server.properties ] || printf 'online-mode=false\nspawn-protection=0\n' > $RUN/server.properties
# Its own port: another test server on this machine may hold 25565 (IAF_PORT=<port> to change)
set_prop server-port "${IAF_PORT:-25581}"
# 26.x pauses a server that has been empty for 60 s (pause-when-empty-seconds): the tests run without players
set_prop pause-when-empty-seconds 0
# A fresh client shows the accessibility (narrator) screen on its first start and waits there: quick play, scenes and
# the net test never get past it. Turned off before the first start.
if [ ! -f $RUN/options.txt ]; then
  printf 'onboardAccessibility:false\nnarrator:0\npauseOnLostFocus:false\ntutorialStep:none\n' > $RUN/options.txt
elif ! grep -q '^onboardAccessibility:false' $RUN/options.txt; then
  perl -pi -e 's/^onboardAccessibility:.*/onboardAccessibility:false/' $RUN/options.txt
  grep -q '^onboardAccessibility:' $RUN/options.txt || echo 'onboardAccessibility:false' >> $RUN/options.txt
fi
