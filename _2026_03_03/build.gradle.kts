plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.12"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "pro.developia"
version = "1.0.0"
description = "_2026_03_03"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 1. Web MVC -> WebFlux 변경
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // 2. Data JPA -> Data R2DBC 변경
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    // 3. MySQL JDBC Driver -> R2DBC MySQL Driver 변경
    runtimeOnly("io.asyncer:r2dbc-mysql:1.1.3") // MySQL 8.x 완벽 지원
    // 4. Coroutines 지원 (WebFlux와 결합)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
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
