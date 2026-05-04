#!/usr/bin/env bash
# Installs project git hooks into .git/hooks/.
# Run once after cloning: bash scripts/install-hooks.sh

set -e

HOOKS_SRC="$(cd "$(dirname "$0")/hooks" && pwd)"
HOOKS_DST="$(cd "$(dirname "$0")/.." && pwd)/.git/hooks"

for hook in "$HOOKS_SRC"/*; do
  name="$(basename "$hook")"
  dest="$HOOKS_DST/$name"

  if [ -f "$dest" ] && ! grep -q "release-check" "$dest" 2>/dev/null; then
    echo "Skipping $name — a custom hook already exists at $dest"
    continue
  fi

  cp "$hook" "$dest"
  chmod +x "$dest"
  echo "Installed $name → .git/hooks/$name"
done

echo "Done. Hooks active for this repository."
