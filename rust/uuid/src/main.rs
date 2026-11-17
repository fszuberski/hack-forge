fn main() {
    let arguments = clap::Command::new("uuid")
        .about("Generate UUIDs")
        .arg(
            clap::Arg::new("version")
                .long("version")
                .help("UUID version to generate")
                .value_parser(["4", "7"])
                .default_value("4"),
        )
        .arg(
            clap::Arg::new("count")
                .long("count")
                .help("Number of UUIDs to generate")
                .value_parser(clap::value_parser!(u64).range(1..))
                .default_value("1"),
        )
        .get_matches();

    let version = arguments
        .get_one::<String>("version")
        .expect("version has a default value");
    let count = arguments
        .get_one::<u64>("count")
        .expect("count has a default value");

    for _ in 0..*count {
        let uuid = if version == "7" {
            uuid::Uuid::now_v7()
        } else {
            uuid::Uuid::new_v4()
        };

        println!("{uuid}");
    }
}
