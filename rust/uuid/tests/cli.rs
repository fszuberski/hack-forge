use std::process::{Command, Output};

use uuid::{Uuid, Variant, Version};

#[test]
fn no_arguments_prints_one_canonical_v4_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::Random);
}

#[test]
fn version_4_prints_one_canonical_v4_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .args(["--version", "4"])
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::Random);
}

#[test]
fn version_7_prints_one_canonical_v7_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .args(["--version", "7"])
        .output()
        .expect("uuid should start");

    assert_uuid_output(output, Version::SortRand);
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

fn assert_uuid_output(output: Output, version: Version) {
    assert!(output.status.success(), "status: {:?}", output.status);
    assert!(output.stderr.is_empty(), "stderr: {:?}", output.stderr);
    assert_eq!(output.stdout.len(), 37, "expected one UUID plus newline");

    let stdout = std::str::from_utf8(&output.stdout).expect("stdout should be UTF-8");
    let text = stdout.strip_suffix('\n').expect("expected a final newline");
    let uuid = Uuid::parse_str(text).expect("output should be a UUID");

    assert_eq!(uuid.get_version(), Some(version));
    assert_eq!(uuid.get_variant(), Variant::RFC4122);
    assert_eq!(text, uuid.hyphenated().to_string());
}
