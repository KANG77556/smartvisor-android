#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CRATE="$ROOT/native/hwp_renderer"
OUT="$ROOT/app/src/main/jniLibs"
: "${ANDROID_NDK_HOME:?ANDROID_NDK_HOME must point to Android NDK}"
rm -rf "$OUT/arm64-v8a" "$OUT/x86_64"
mkdir -p "$OUT"
cd "$CRATE"
cargo ndk --platform 26 -t arm64-v8a -t x86_64 -o "$OUT" build --release
test -f "$OUT/arm64-v8a/libhwp_renderer.so"
test -f "$OUT/x86_64/libhwp_renderer.so"
