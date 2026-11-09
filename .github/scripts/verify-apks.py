#!/usr/bin/env python3
"""Check the eight CI APKs before publishing installable artifacts."""

import argparse
import os
from pathlib import Path
import re
import struct
import subprocess
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
ABIS = {"arm64-v8a": (2, 183), "armeabi-v7a": (1, 40), "x86_64": (2, 62), "x86": (1, 3)}
NATIVE_LIBS = {"libmihomo.so", "libmishka.so", "libmishka_jni.so", "libmihomo_runner.so"}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--signed", action="store_true", help="Require the pinned signing certificate")
    args = parser.parse_args()
    sdk = Path(os.environ["ANDROID_HOME"]) / "build-tools" / "36.0.0"
    certificate = (ROOT / ".github/ci/signing-certificate.sha256").read_text().strip()
    config = (ROOT / "build-logic/src/main/kotlin/ProjectConfig.kt").read_text()
    version = re.search(r'const val VERSION_NAME = "([^"]+)"', config).group(1)
    version_code = os.environ["CI_VERSION_CODE"]
    for variant in ("debug", "release"):
        directory = ROOT / "app/build/outputs/apk" / variant
        apks = list(directory.glob("*.apk"))
        if len(apks) != len(ABIS):
            raise SystemExit(f"Expected four {variant} APKs, found {len(apks)}")
        for abi, (elf_class, machine) in ABIS.items():
            matches = [apk for apk in apks if f"-{abi}-" in apk.name]
            if len(matches) != 1:
                raise SystemExit(f"Expected one {variant}/{abi} APK")
            apk = matches[0]
            badging = subprocess.check_output([sdk / "aapt2", "dump", "badging", apk], text=True)
            package_line = badging.splitlines()[0]
            for expected in ("name='rinn.rei.mihane'", f"versionCode='{version_code}'", f"versionName='{version}'"):
                if expected not in package_line:
                    raise SystemExit(f"{apk.name}: incorrect package metadata: {package_line}")
            with ZipFile(apk) as archive:
                native_abis = {name.split('/')[1] for name in archive.namelist() if name.startswith('lib/') and name.endswith('.so')}
                if native_abis != {abi}:
                    raise SystemExit(f"{apk.name}: incorrect ABI set {native_abis}")
                for library in NATIVE_LIBS:
                    with archive.open(f"lib/{abi}/{library}") as source:
                        header = source.read(20)
                    if header[:4] != b"\x7fELF" or header[4] != elf_class or header[5] != 1 or struct.unpack_from('<H', header, 18)[0] != machine:
                        raise SystemExit(f"{apk.name}: incorrect ELF architecture for {library}")
                for notice in ("misans.txt", "outfit.txt", "gpl-3.0.txt"):
                    if f"assets/licenses/{notice}" not in archive.namelist():
                        raise SystemExit(f"{apk.name}: missing {notice}")
            if args.signed:
                result = subprocess.check_output([sdk / "apksigner", "verify", "--print-certs", apk], text=True)
                digests = re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)', result)
                if [value.lower() for value in digests] != [certificate]:
                    raise SystemExit(f"{apk.name}: signing certificate mismatch")
            print(f"Verified {variant}/{abi}: {apk.name}")


if __name__ == "__main__":
    main()
