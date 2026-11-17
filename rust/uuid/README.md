# UUID CLI

From the repository root, generate one UUID:

```sh
cargo run --quiet --manifest-path rust/uuid/Cargo.toml
```

With no arguments, `uuid` prints one lowercase, hyphenated RFC v4 UUID followed
by a newline. Successful execution exits with status zero and no stderr output.

Select UUID version 4 or 7 with `--version`:

```sh
cargo run --quiet --manifest-path rust/uuid/Cargo.toml -- --version 4
cargo run --quiet --manifest-path rust/uuid/Cargo.toml -- --version 7
```

`--version` selects the UUID version, not the application version. Either choice
prints exactly one UUID unless `--count` is supplied.

Generate multiple UUIDs with `--count`, a positive integer defaulting to one:

```sh
cargo run --quiet --manifest-path rust/uuid/Cargo.toml -- --count 3
cargo run --quiet --manifest-path rust/uuid/Cargo.toml -- --version 7 --count 3
cargo run --quiet --manifest-path rust/uuid/Cargo.toml -- --count 3 --version 4
```

Count alone retains v4; `--count` and `--version` can appear in either order.
Each UUID is generated separately and printed as one lowercase, hyphenated RFC
UUID per line, including a final newline. Successful execution exits with status
zero and no stderr output.

Counts must fit an unsigned 64-bit integer (1 through 18446744073709551615).
Zero, negative, nonnumeric, fractional, overflowing, or missing count values are
rejected before any UUID is printed. Unsupported versions, missing version
values, and unrecognized arguments are also rejected. Invalid invocations exit
unsuccessfully with a stderr diagnostic and no stdout.
Use `--help` for usage information.

## Build a native executable

With Rust/Cargo installed, run from the repository root:

```sh
artifact=$(./rust/uuid/build.sh)
"$artifact"
"$artifact" --version 7 --count 3
```

The executable script also works by absolute path from any working directory.
It runs `cargo build --release --locked` for this crate and copies the optimized
native executable to `rust/uuid/target/release/uuid.bin` by default, preserving
executable permissions. This is a native Linux/Unix executable, not an archive
or a cross-compiled package; nothing is installed on the host.

Set `CARGO_TARGET_DIR` to choose another output directory (quote paths with
spaces). Relative target directories are resolved from the caller's working
directory, not the crate directory:

```sh
artifact=$(CARGO_TARGET_DIR="/tmp/uuid output" ./rust/uuid/build.sh)
"$artifact" --count 3
```

On success stdout contains only the resulting absolute `.bin` path; Cargo logs
go to stderr. Build or copy failures exit nonzero without printing a success path.

## Tests

Run the CLI and build-script integration tests:

```sh
cargo test --manifest-path rust/uuid/Cargo.toml
```

The Unix build tests invoke real Cargo release builds and execute the packaged
binary. They use isolated directories in the system temporary directory
(honoring `TMPDIR`), reusing `CARGO_TARGET_DIR` as the build cache when set or
an isolated cache otherwise, and remove their temporary directories afterward.
