#!/usr/bin/env sh
# Lightweight Gradle 8.2.1 bootstrap for repositories that did not include
# the standard Gradle Wrapper JAR. The distribution is downloaded from the
# official Gradle service and cached under GRADLE_USER_HOME.
set -eu

GRADLE_VERSION='8.2.1'
GRADLE_DIST="gradle-${GRADLE_VERSION}-bin"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-${HOME}/.gradle}"
INSTALL_DIR="${GRADLE_USER_HOME}/wrapper/dists/${GRADLE_DIST}/mind-bootstrap"
GRADLE_BIN="${INSTALL_DIR}/gradle-${GRADLE_VERSION}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$INSTALL_DIR"
    ZIP_FILE="${INSTALL_DIR}/${GRADLE_DIST}.zip"
    TEMP_ZIP="${ZIP_FILE}.part"
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --retry 3 --retry-delay 2 \
            "https://services.gradle.org/distributions/${GRADLE_DIST}.zip" \
            --output "$TEMP_ZIP"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$TEMP_ZIP" \
            "https://services.gradle.org/distributions/${GRADLE_DIST}.zip"
    else
        echo "Error: curl or wget is required to download Gradle." >&2
        exit 1
    fi
    mv "$TEMP_ZIP" "$ZIP_FILE"
    if ! command -v unzip >/dev/null 2>&1; then
        echo "Error: unzip is required to install Gradle." >&2
        exit 1
    fi
    unzip -q -o "$ZIP_FILE" -d "$INSTALL_DIR"
    rm -f "$ZIP_FILE"
fi

exec "$GRADLE_BIN" "$@"
