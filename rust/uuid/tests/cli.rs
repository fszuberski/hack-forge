use std::process::{Command, Output};

use uuid::{Uuid, Variant, Version};

#[test]
fn no_arguments_prints_one_canonical_v4_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::Random, 1);
}

#[test]
fn version_4_prints_one_canonical_v4_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .args(["--version", "4"])
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::Random, 1);
}

#[test]
fn version_7_prints_one_canonical_v7_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .args(["--version", "7"])
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::SortRand, 1);
}

#[test]
fn count_prints_requested_canonical_uuids_with_default_or_explicit_version() {
    let cases: &[(&[&str], Version, usize)] = &[
        (&["--count", "1"], Version::Random, 1),
        (&["--count", "3"], Version::Random, 3),
        (&["--count", "1", "--version", "4"], Version::Random, 1),
        (&["--count", "3", "--version", "4"], Version::Random, 3),
        (&["--version", "4", "--count", "1"], Version::Random, 1),
        (&["--version", "4", "--count", "3"], Version::Random, 3),
        (&["--count", "1", "--version", "7"], Version::SortRand, 1),
        (&["--count", "3", "--version", "7"], Version::SortRand, 3),
        (&["--version", "7", "--count", "1"], Version::SortRand, 1),
        (&["--version", "7", "--count", "3"], Version::SortRand, 3),
    ];

    for (args, version, count) in cases {
        let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
            .args(*args)
            .output()
            .expect("uuid should start");

        assert_uuid_output(output, *version, *count);
    }
}

#[test]
fn invalid_arguments_fail_with_a_diagnostic_and_no_stdout() {
    let cases: &[&[&str]] = &[
        &["--version", "5"],
        &["--version", "abc"],
        &["--version"],
        &["--unknown"],
        &["extra"],
        &["--version", "7", "extra"],
        &["--count", "0"],
        &["--count", "-1"],
        &["--count", "abc"],
        &["--count", "1.5"],
        &["--count"],
        &["--count", "18446744073709551616"],
        &["--version", "7", "--count", "0"],
        &["--count", "3", "--version", "5"],
    ];

    for args in cases {
        let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
            .args(*args)
            .output()
            .expect("uuid should start");

        assert!(
            !output.status.success(),
            "arguments {args:?} should fail, status: {:?}",
            output.status
        );
        assert!(
            output.stdout.is_empty(),
            "arguments {args:?} should not print stdout: {:?}",
            output.stdout
        );
        assert!(
            !output.stderr.is_empty(),
            "arguments {args:?} should print a diagnostic"
        );
    }
}

fn assert_uuid_output(output: Output, version: Version, count: usize) {
    assert!(
        output.status.success(),
        "status: {:?}, stderr: {}",
        output.status,
        String::from_utf8_lossy(&output.stderr)
    );
    assert!(output.stderr.is_empty(), "stderr: {:?}", output.stderr);
    assert_eq!(
        output.stdout.len(),
        37 * count,
        "expected {count} UUIDs, each followed by a newline"
    );

    let stdout = std::str::from_utf8(&output.stdout).expect("stdout should be UTF-8");
    let text = stdout.strip_suffix('\n').expect("expected a final newline");
    let lines: Vec<_> = text.split('\n').collect();
    assert_eq!(lines.len(), count);

    for line in &lines {
        let uuid = Uuid::parse_str(line).expect("each line should be a UUID");

        assert_eq!(uuid.get_version(), Some(version));
        assert_eq!(uuid.get_variant(), Variant::RFC4122);
        assert_eq!(*line, uuid.hyphenated().to_string());
    }

    if count > 1 {
        assert!(
            lines.iter().any(|line| *line != lines[0]),
            "the sample should not repeat one generated UUID"
        );
    }
}
