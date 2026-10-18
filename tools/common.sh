# Shared helpers of the test scripts, the same on Windows (Git Bash) and macOS/Linux.
#   kill_runs <regex>        stop java processes of this project whose command line matches <regex> (not the Gradle daemon)
#   timed <seconds> cmd...   run cmd with a time limit (no GNU timeout on macOS)
#   set_prop <key> <value>   set a key in run/server.properties (creates the file)
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
  mkdir -p run
  touch run/server.properties
  if grep -q "^$1=" run/server.properties; then
    perl -pi -e "s/^\Q$1\E=.*/$1=$2/" run/server.properties
  else
    echo "$1=$2" >> run/server.properties
  fi
}

# A fresh checkout has no run/ (it is ignored): the test server needs the EULA (accepted by the owner) and a test setup
mkdir -p run
[ -f run/eula.txt ] || echo 'eula=true' > run/eula.txt
[ -f run/server.properties ] || printf 'online-mode=false\nspawn-protection=0\n' > run/server.properties
# Its own port: another test server on this machine may hold 25565 (IAF_PORT=<port> to change)
set_prop server-port "${IAF_PORT:-25581}"
# A fresh client shows the accessibility (narrator) screen on its first start and waits there: quick play, scenes and
# the net test never get past it. Turned off before the first start.
if [ ! -f run/options.txt ]; then
  printf 'onboardAccessibility:false\nnarrator:0\npauseOnLostFocus:false\ntutorialStep:none\n' > run/options.txt
elif ! grep -q '^onboardAccessibility:false' run/options.txt; then
  perl -pi -e 's/^onboardAccessibility:.*/onboardAccessibility:false/' run/options.txt
  grep -q '^onboardAccessibility:' run/options.txt || echo 'onboardAccessibility:false' >> run/options.txt
fi
