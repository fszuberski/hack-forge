#![cfg(unix)]

use std::path::{Path, PathBuf};
use std::process::{Command, Output};
use std::time::{SystemTime, UNIX_EPOCH};

use uuid::{Uuid, Variant, Version};

#[test]
fn builds_a_native_executable_from_another_directory_with_absolute_and_relative_targets() {
    let directory = TestDirectory::new();
    let cache = std::env::var_os("CARGO_TARGET_DIR")
        .map(PathBuf::from)
        .unwrap_or_else(|| directory.0.join("cache"));
    let cache = std::path::absolute(cache).expect("target cache should resolve");
    std::fs::create_dir_all(&cache).expect("target cache should exist");
    let target = directory.0.join("target cache");
    std::os::unix::fs::symlink(cache, &target).expect("target cache link should be created");

    for target_dir in [target.as_path(), Path::new("target cache")] {
        let output = build_command(&directory.0)
            .env("CARGO_TARGET_DIR", target_dir)
            .output()
            .expect("build script should start");

        assert!(
            output.status.success(),
            "status: {:?}, stderr: {}",
            output.status,
            String::from_utf8_lossy(&output.stderr)
        );
        let stdout = std::str::from_utf8(&output.stdout).expect("build output should be UTF-8");
        let path = stdout.strip_suffix('\n').expect("expected artifact path");
        assert!(!path.contains('\n'), "stdout should contain only one path");
        let artifact = directory.0.join(path);
        assert_eq!(artifact.file_name().unwrap(), "uuid.bin");
        assert!(artifact.is_file(), "artifact should exist: {artifact:?}");
        assert_eq!(
            artifact.canonicalize().unwrap(),
            target.join("release/uuid.bin").canonicalize().unwrap()
        );

        for (args, version, count) in [
            (vec![], Version::Random, 1),
            (vec!["--version", "7", "--count", "3"], Version::SortRand, 3),
        ] {
            let output = Command::new(&artifact)
                .args(args)
                .output()
                .expect("packaged native executable should start");
            assert_uuid_output(output, version, count);
        }
    }
}

#[test]
fn cargo_failure_exits_unsuccessfully_without_printing_an_artifact_path() {
    let directory = TestDirectory::new();
    let output = build_command(&directory.0)
        .env("CARGO_TARGET_DIR", directory.0.join("failed target"))
        .env("RUSTC", directory.0.join("nonexistent-rustc"))
        .output()
        .expect("build script should start");

    assert!(!output.status.success(), "missing compiler should fail");
    assert!(output.stdout.is_empty(), "failure must not print a path");
    assert!(
        !output.stderr.is_empty(),
        "failure should have a diagnostic"
    );
    assert!(!directory.0.join("failed target/release/uuid.bin").exists());
}

fn build_command(cwd: &Path) -> Command {
    let mut command = Command::new(Path::new(env!("CARGO_MANIFEST_DIR")).join("build.sh"));
    command.current_dir(cwd);
    command
}

fn assert_uuid_output(output: Output, version: Version, count: usize) {
    assert!(output.status.success(), "status: {:?}", output.status);
    assert!(output.stderr.is_empty(), "stderr: {:?}", output.stderr);
    assert_eq!(output.stdout.len(), 37 * count);
    let stdout = std::str::from_utf8(&output.stdout).expect("UUID output should be UTF-8");
    assert!(stdout.ends_with('\n'));
    let lines: Vec<_> = stdout.lines().collect();
    assert_eq!(lines.len(), count);
    for line in lines {
        let uuid = Uuid::parse_str(line).expect("each line should be a UUID");
        assert_eq!(uuid.get_version(), Some(version));
        assert_eq!(uuid.get_variant(), Variant::RFC4122);
        assert_eq!(line, uuid.hyphenated().to_string());
    }
}

struct TestDirectory(PathBuf);

impl TestDirectory {
    fn new() -> Self {
        let nonce = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("system time should follow the epoch")
            .as_nanos();
        let path = std::env::temp_dir().join(format!(
            "hack-forge-uuid-build-{}-{nonce} caller with spaces",
            std::process::id()
        ));
        std::fs::create_dir(&path).expect("isolated test directory should be created");
        Self(path)
    }
}

impl Drop for TestDirectory {
    fn drop(&mut self) {
        std::fs::remove_dir_all(&self.0).expect("owned test directory should be removed");
    }
}
