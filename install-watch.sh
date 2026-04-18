#!/usr/bin/env bash

set -euo pipefail

PACKAGE_NAME="com.cgm.timetwist"
DEFAULT_VARIANT="debug"

usage() {
    cat <<'EOF'
Usage: ./install-watch.sh [debug|release] [--device DEVICE_ID]

Builds the selected variant, uninstalls any existing TimeTwist app from the
target watch, and installs the newly built APK.

Examples:
  ./install-watch.sh
  ./install-watch.sh release
  ./install-watch.sh debug --device R3CX12345AB
EOF
}

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
variant="$DEFAULT_VARIANT"
device_id=""

while (($# > 0)); do
    case "$1" in
        debug|release)
            variant="$1"
            shift
            ;;
        --device)
            if (($# < 2)); then
                echo "Missing value for --device" >&2
                usage
                exit 1
            fi
            device_id="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage
            exit 1
            ;;
    esac
done

find_adb() {
    if command -v adb >/dev/null 2>&1; then
        command -v adb
        return
    fi

    if [[ -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb" ]]; then
        printf '%s\n' "${ANDROID_HOME}/platform-tools/adb"
        return
    fi

    if [[ -n "${ANDROID_SDK_ROOT:-}" && -x "${ANDROID_SDK_ROOT}/platform-tools/adb" ]]; then
        printf '%s\n' "${ANDROID_SDK_ROOT}/platform-tools/adb"
        return
    fi

    local sdk_dir=""
    if [[ -f "${repo_root}/local.properties" ]]; then
        sdk_dir="$(sed -n 's/^sdk\.dir=//p' "${repo_root}/local.properties" | tail -n 1)"
    fi

    if [[ -n "${sdk_dir}" && -x "${sdk_dir}/platform-tools/adb" ]]; then
        printf '%s\n' "${sdk_dir}/platform-tools/adb"
        return
    fi

    return 1
}

adb_bin="$(find_adb)" || {
    echo "Unable to find adb. Install Android platform-tools or set ANDROID_HOME/ANDROID_SDK_ROOT." >&2
    exit 1
}

adb_cmd=("${adb_bin}")
if [[ -n "${device_id}" ]]; then
    adb_cmd+=(-s "${device_id}")
fi

if [[ -z "${device_id}" ]]; then
    mapfile -t connected_devices < <("${adb_bin}" devices | awk 'NR > 1 && $2 == "device" { print $1 }')

    if ((${#connected_devices[@]} == 0)); then
        echo "No connected adb devices found." >&2
        exit 1
    fi

    if ((${#connected_devices[@]} > 1)); then
        echo "Multiple adb devices detected. Re-run with --device DEVICE_ID." >&2
        printf 'Connected devices:\n' >&2
        printf '  %s\n' "${connected_devices[@]}" >&2
        exit 1
    fi

    device_id="${connected_devices[0]}"
    adb_cmd+=(-s "${device_id}")
fi

if [[ "${variant}" == "debug" ]]; then
    gradle_task="assembleDebug"
    apk_path="${repo_root}/app/build/outputs/apk/debug/app-debug.apk"
else
    gradle_task="assembleRelease"
    apk_path="${repo_root}/app/build/outputs/apk/release/app-release.apk"
fi

echo "Using adb: ${adb_bin}"
echo "Target device: ${device_id}"
echo "Building ${variant} APK..."
(cd "${repo_root}" && ./gradlew "${gradle_task}")

if [[ ! -f "${apk_path}" ]]; then
    echo "APK not found at ${apk_path}" >&2
    exit 1
fi

echo "Uninstalling existing ${PACKAGE_NAME} if present..."
"${adb_cmd[@]}" uninstall "${PACKAGE_NAME}" >/dev/null 2>&1 || true

echo "Installing ${apk_path}..."
"${adb_cmd[@]}" install -r "${apk_path}"

echo "Install complete."
