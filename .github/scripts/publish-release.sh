#!/usr/bin/env bash
set -euo pipefail

: "${GITHUB_REPOSITORY:?Missing GITHUB_REPOSITORY}"
: "${RELEASE_VERSION:?Missing RELEASE_VERSION}"
: "${RELEASE_VERSION_CODE:?Missing RELEASE_VERSION_CODE}"
[[ "$RELEASE_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$ ]]
[[ "$RELEASE_VERSION_CODE" =~ ^[0-9]+$ ]]

directory="$(cd -- "${1:?Provide the Release APK directory}" && pwd)"
tag="v$RELEASE_VERSION"
commit="$(git rev-parse HEAD)"
if git show-ref --verify --quiet "refs/tags/$tag"; then
    [[ "$(git rev-parse "$tag^{commit}")" == "$commit" ]] || {
        echo "$tag already points to a different commit" >&2
        exit 1
    }
fi

shopt -s nullglob
apks=("$directory"/*.apk)
[[ ${#apks[@]} == 4 ]] || { echo "Expected four Release APKs" >&2; exit 1; }
for abi in arm64-v8a armeabi-v7a x86_64 x86; do
    expected="$directory/mihane-v$RELEASE_VERSION($RELEASE_VERSION_CODE)-$abi-release.apk"
    [[ -s "$expected" ]] || { echo "Missing $abi Release APK" >&2; exit 1; }
done
(
    cd -- "$directory"
    sha256sum -- *.apk > SHA256SUMS
    sha256sum --check --strict SHA256SUMS
)

notes="$(mktemp)"
trap 'rm -f -- "$notes"' EXIT
cat > "$notes" <<EOF
Mihane $RELEASE_VERSION

支持 Android 12 及以上系统。按设备架构下载 APK，常见手机和平板选择 arm64-v8a。

版本号：$RELEASE_VERSION_CODE
EOF
options=(--repo "$GITHUB_REPOSITORY" --target "$commit" --title "Mihane $RELEASE_VERSION" --notes-file "$notes")
if [[ "$RELEASE_VERSION" == *-* ]]; then
    options+=(--prerelease)
fi
gh release create "$tag" "${apks[@]}" "$directory/SHA256SUMS" "${options[@]}"
