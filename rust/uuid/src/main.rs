fn main() {
    let arguments = clap::Command::new("uuid")
        .about("Generate a UUID")
        .arg(
            clap::Arg::new("version")
                .long("version")
                .help("UUID version to generate")
                .value_parser(["4", "7"])
                .default_value("4"),
        )
        .get_matches();

    let version = arguments
        .get_one::<String>("version")
        .expect("version has a default value");
    let uuid = if version == "7" {
        uuid::Uuid::now_v7()
    } else {
        uuid::Uuid::new_v4()
    };

    println!("{uuid}");
}
