#!/usr/bin/env bash
# DLaunch Automated Native Build Pipeline
# Builds, dexes, aligns, and signs dlaunch.apk with zero bloated dependencies.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  Building DLaunch TV Launcher"
echo "=========================================="

BUILD_TOOLS_DIR="/opt/homebrew/share/android-commandlinetools/build-tools/34.0.0"
PLATFORM_JAR="/opt/homebrew/share/android-commandlinetools/platforms/android-34/android.jar"

AAPT2="$BUILD_TOOLS_DIR/aapt2"
D8="$BUILD_TOOLS_DIR/d8"
ZIPALIGN="$BUILD_TOOLS_DIR/zipalign"
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"

if [ ! -f "$AAPT2" ] || [ ! -f "$PLATFORM_JAR" ]; then
    echo "Error: Android build tools or platform jar not found!"
    exit 1
fi

BUILD_DIR="$SCRIPT_DIR/build"
GEN_DIR="$BUILD_DIR/gen"
OBJ_DIR="$BUILD_DIR/obj"
COMPILED_RES_DIR="$BUILD_DIR/compiled_res"

rm -rf "$BUILD_DIR"
mkdir -p "$GEN_DIR" "$OBJ_DIR" "$COMPILED_RES_DIR"

echo "1. Compiling resources with aapt2..."
"$AAPT2" compile --dir res -o "$COMPILED_RES_DIR/res.zip"

echo "2. Linking resources and generating R.java..."
"$AAPT2" link "$COMPILED_RES_DIR/res.zip" \
    -I "$PLATFORM_JAR" \
    --manifest AndroidManifest.xml \
    --java "$GEN_DIR" \
    -o "$BUILD_DIR/dlaunch_unaligned.apk" \
    --auto-add-overlay

echo "3. Compiling Java sources with javac..."
javac --release 8 \
    -cp "$PLATFORM_JAR" \
    -d "$OBJ_DIR" \
    $(find "$GEN_DIR" "$SCRIPT_DIR/src" -name "*.java")

echo "4. Converting bytecode to Dalvik Executable (classes.dex) with d8..."
"$D8" --min-api 21 \
    --lib "$PLATFORM_JAR" \
    --output "$BUILD_DIR" \
    $(find "$OBJ_DIR" -name "*.class")

echo "5. Packaging classes.dex into APK..."
cd "$BUILD_DIR"
jar uf "dlaunch_unaligned.apk" classes.dex
cd "$SCRIPT_DIR"

echo "6. Aligning APK with zipalign..."
"$ZIPALIGN" -f -p 4 "$BUILD_DIR/dlaunch_unaligned.apk" "$BUILD_DIR/dlaunch_aligned.apk"

echo "7. Signing APK with apksigner..."
KEYSTORE="$BUILD_DIR/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkeypair \
        -keystore "$KEYSTORE" \
        -storepass android \
        -alias androiddebugkey \
        -keypass android \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -dname "CN=DLaunch, OU=DewZone, O=UnbloatTV, C=US"
fi

"$APKSIGNER" sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --ks-key-alias androiddebugkey \
    --key-pass pass:android \
    --out "$SCRIPT_DIR/dlaunch.apk" \
    "$BUILD_DIR/dlaunch_aligned.apk"

# Also copy a copy into tools/dlaunch.apk for easy 1-click install
mkdir -p "$SCRIPT_DIR/../tools"
cp -f "$SCRIPT_DIR/dlaunch.apk" "$SCRIPT_DIR/../tools/dlaunch.apk"

# Verify signature
"$APKSIGNER" verify "$SCRIPT_DIR/dlaunch.apk"

APK_SIZE=$(ls -lh "$SCRIPT_DIR/dlaunch.apk" | awk '{print $5}')
echo "=========================================="
echo "  SUCCESS! DLaunch built: dlaunch.apk ($APK_SIZE)"
echo "=========================================="
