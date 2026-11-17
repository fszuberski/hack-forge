# UUID CLI

From the repository root, generate one UUID:

```sh
cargo run --quiet --manifest-path rust/uuid/Cargo.toml
```

With no arguments, `uuid` prints one lowercase, hyphenated RFC v4 UUID followed
by a newline. Successful execution exits with status zero and no stderr output.
There are no command-line options yet.

Run the CLI integration test:

```sh
cargo test --manifest-path rust/uuid/Cargo.toml
```
