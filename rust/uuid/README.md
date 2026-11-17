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

Run the CLI integration tests:

```sh
cargo test --manifest-path rust/uuid/Cargo.toml
```
