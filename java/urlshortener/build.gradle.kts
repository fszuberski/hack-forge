plugins {
	java
	id("org.springframework.boot") version "3.2.12"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.fszuberski"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)

	implementation(libs.commons.codec)
	implementation(libs.commons.lang3)
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
