plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "pro.developia"
version = "0.0.1-SNAPSHOT"
description = "_2026_07"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // 코루틴 핵심. 버전은 Spring Boot dependency-management(kotlinx-coroutines 모듈 관리)에 위임.
    // 만약 빌드가 "version required"로 실패하면 kotlinx-coroutines-bom을 platform()으로 명시하거나
    // 두 아티팩트에 동일 버전을 핀 고정하면 됨(-core 와 -test 버전은 항상 일치시킬 것).
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 3주차 backpressure 벤치마크 실행:  ./gradlew benchmark
tasks.register<JavaExec>("benchmark") {
    group = "application"
    description = "backpressure 전략별 처리량/지연/드롭 측정"
    mainClass.set("_2026_07.benchmark.BackpressureBenchmarkKt")
    classpath = sourceSets["main"].runtimeClasspath
}

// Spring 없이 순수 코루틴 파이프라인 데모:  ./gradlew standaloneDemo
tasks.register<JavaExec>("standaloneDemo") {
    group = "application"
    description = "프레임워크 독립 파이프라인 데모(traceId 로그 포함)"
    mainClass.set("_2026_07.demo.StandaloneDemoKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("prepareKotlinBuildScriptModel") {}
