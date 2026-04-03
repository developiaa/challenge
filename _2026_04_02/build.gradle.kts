plugins {
    id("org.springframework.boot") version "3.5.13"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
}

group = "pro.developia"
version = "1.0.0"
description = "_2026_04_02"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    val exposedVersion = "0.50.0"
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Exposed 구동을 위한 JDBC 드라이버
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("com.h2database:h2")

    testImplementation("io.kotest:kotest-assertions-core:5.8.1")

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


tasks.register("prepareKotlinBuildScriptModel") {}
