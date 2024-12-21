plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "paket"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Core
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Security for authentication/authorization
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Jackson for JSON support with Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JWT for token generation and validation
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // BCrypt for password hashing
    implementation("org.springframework.security:spring-security-crypto")

    // Kotlin-specific dependencies
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    // Database Driver (replace with MariaDB or PostgreSQL as per your DB choice)
    runtimeOnly("org.postgresql:postgresql")
    // Or MariaDB if that's your choice
    // runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // Spring Session (if needed for token management)
    implementation("org.springframework.session:spring-session-core")

    // Arrow
    implementation("io.arrow-kt:arrow-core:1.2.0")

    // Development tools
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
