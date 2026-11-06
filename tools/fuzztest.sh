#!/bin/sh
# Bug hunt (Fuzz in fabric/src/selftest) on a fresh world with the compat mods, Lithium among them (a server test without them missed a
# crash that every player with Lithium had). Fabric only. FUZZ_RADIUS=15: many more new chunks with every chance of the mod at 1.
cd "$(dirname "$0")/.."
FUZZ_OPTS="-Diceandfire.fuzz=true -Diceandfire.fuzz.radius=${FUZZ_RADIUS:-3}"
JAVA_TOOL_OPTIONS="$FUZZ_OPTS" LOADER=fabric MAX_TICK_TIME="${MAX_TICK_TIME:-600000}" STALL="${STALL:-900}" sh tools/selftest.sh -Pwith_compat "$@" > /dev/null
grep -a '\[fuzz\]' build-selftest.log | grep -v 'worldgen waiting' | sed -E 's/^.*\[fuzz\] //' | cut -c1-300
grep -a -q '\[fuzz\] DONE .* 0 distinct' build-selftest.log
