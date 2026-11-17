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
prints exactly one lowercase, hyphenated RFC UUID followed by a newline.
Unsupported versions, nonnumeric values, missing values, and unrecognized
arguments exit unsuccessfully with a stderr diagnostic and no stdout.
Use `--help` for usage information.

Run the CLI integration tests:

```sh
cargo test --manifest-path rust/uuid/Cargo.toml
```
