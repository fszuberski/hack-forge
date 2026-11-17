use std::process::Command;

use uuid::{Uuid, Variant, Version};

#[test]
fn no_arguments_prints_one_canonical_v4_uuid() {
    let output = Command::new(env!("CARGO_BIN_EXE_uuid"))
        .output()
        .expect("uuid should start");

    assert!(output.status.success(), "status: {:?}", output.status);
    assert!(output.stderr.is_empty(), "stderr: {:?}", output.stderr);
    assert_eq!(output.stdout.len(), 37, "expected one UUID plus newline");

    let stdout = std::str::from_utf8(&output.stdout).expect("stdout should be UTF-8");
    let text = stdout.strip_suffix('\n').expect("expected a final newline");
    let uuid = Uuid::parse_str(text).expect("output should be a UUID");

    assert_eq!(uuid.get_version(), Some(Version::Random));
    assert_eq!(uuid.get_variant(), Variant::RFC4122);
    assert_eq!(text, uuid.hyphenated().to_string());
}
