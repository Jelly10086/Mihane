#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
: "${MISANS_ARCHIVE:?Set MISANS_ARCHIVE to the destination ZIP path}"
mkdir -p -- "$(dirname -- "$MISANS_ARCHIVE")"
partial="$(mktemp "${MISANS_ARCHIVE}.XXXXXX")"
trap 'rm -f -- "$partial"' EXIT

curl --fail --location --retry 3 --retry-delay 2 \
    --connect-timeout 15 --max-time 600 --proto '=https' --proto-redir '=https' \
    --output "$partial" https://hyperos.mi.com/font-download/MiSans.zip
printf '%s  %s\n' "$(< "$repo_dir/fonts/misans-archive.sha256")" "$partial" | sha256sum --check --strict
python3 - "$partial" "$repo_dir/fonts/misans.sha256" <<'PYTHON'
import hashlib
import sys
from pathlib import Path
from zipfile import ZipFile

with ZipFile(sys.argv[1]) as archive:
    fonts = [item for item in archive.infolist()
             if not item.is_dir() and not item.filename.startswith("__MACOSX/")
             and item.filename.endswith("/MiSansVF.ttf")]
    if len(fonts) != 1:
        raise SystemExit("Expected exactly one MiSansVF.ttf in the official archive")
    with archive.open(fonts[0]) as font:
        actual = hashlib.file_digest(font, "sha256").hexdigest()
    if actual != Path(sys.argv[2]).read_text().strip():
        raise SystemExit("MiSansVF.ttf SHA-256 mismatch")
    print(f"MiSansVF.ttf verified: {actual}")
PYTHON
mv -f -- "$partial" "$MISANS_ARCHIVE"
