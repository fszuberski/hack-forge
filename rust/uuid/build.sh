#!/bin/sh
set -eu

script_dir=$(CDPATH= cd "$(dirname "$0")" && pwd)
target_dir=${CARGO_TARGET_DIR:-"$script_dir/target"}

cargo build --release --locked --manifest-path "$script_dir/Cargo.toml" >&2

target_dir=$(CDPATH= cd "$target_dir" && pwd)
artifact="$target_dir/release/uuid.bin"
if [ -d "$artifact" ]; then
    printf 'Artifact path is a directory: %s\n' "$artifact" >&2
    exit 1
fi
cp -p "$target_dir/release/uuid" "$artifact"
printf '%s\n' "$artifact"
