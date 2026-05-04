#!/usr/bin/env bash
# docs-check.sh — CI verification that documentation stays in sync with code.
#
# Checks:
#   1. README.md "Current version" matches app/build.gradle.kts versionName
#   2. AGENTS.md project structure mentions every top-level source directory
#   3. Every .kt file has a parent directory listed in AGENTS.md
#
# Returns 0 if all checks pass, 1 if any fail.

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
RESET='\033[0m'

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FAILED=0

# ── Check 1: versionName in build.gradle.kts matches README.md ─────────────────

VERSION_GRADLE=$(grep 'versionName' "$ROOT/app/build.gradle.kts" | grep -o '"[^"]*"' | tr -d '"')
VERSION_README=$(grep 'Current version' "$ROOT/README.md" | grep -o '\*\*[^*]*\*\*' | sed 's/\*\*//g')

if [ "$VERSION_GRADLE" != "$VERSION_README" ]; then
  echo -e "${RED}[FAIL]${RESET} versionName mismatch: build.gradle.kts=${VERSION_GRADLE}, README.md=${VERSION_README}"
  FAILED=1
else
  echo -e "${GREEN}[PASS]${RESET} versionName matches: ${VERSION_GRADLE}"
fi

# ── Check 2: AGENTS.md lists all source directories ────────────────────────────

SRC_ROOT="$ROOT/app/src/main/java/com/example/expense"
# Gather all top-level source directories (exclude files at root like ExpenseApp.kt, MainActivity.kt)
SRC_DIRS=$(find "$SRC_ROOT" -mindepth 1 -maxdepth 3 -type d -not -path '*/build/*' | while read -r d; do
  rel="${d#$SRC_ROOT/}"
  # Only report directories that contain .kt files
  if ls "$d"/*.kt >/dev/null 2>&1; then
    echo "$rel"
  fi
done | sort -u)

MISSING_DIRS=""
while IFS= read -r dir; do
  [ -z "$dir" ] && continue
  leaf="$(basename "$dir")/"
  if ! grep -q "$leaf" "$ROOT/AGENTS.md"; then
    MISSING_DIRS="$MISSING_DIRS  $dir\n"
  fi
done <<< "$SRC_DIRS"

if [ -n "$MISSING_DIRS" ]; then
  echo -e "${RED}[FAIL]${RESET} Source directories not in AGENTS.md:"
  echo -e "$MISSING_DIRS"
  FAILED=1
else
  echo -e "${GREEN}[PASS]${RESET} All source directories listed in AGENTS.md"
fi

# ── Final ──────────────────────────────────────────────────────────────────────
if [ "$FAILED" -ne 0 ]; then
  echo -e "\n${RED}[FAIL]${RESET} Documentation needs updating. See above."
  exit 1
else
  echo -e "\n${GREEN}[PASS]${RESET} Documentation is in sync with code."
  exit 0
fi
