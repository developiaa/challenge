import com.google.protobuf.gradle.*

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"

    id("com.google.protobuf") version "0.9.4"
}

group = "pro.developia"
version = "0.0.1-SNAPSHOT"
description = "_2026_05_02"

// gRPC 및 Protobuf 버전 정의
val grpcVersion = "1.58.0"
val grpcKotlinVersion = "1.4.0"
val protobufVersion = "3.24.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    // 1. gRPC Core & Protobuf
    implementation("io.grpc:grpc-netty-shaded:${grpcVersion}")
    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")

    // 2. gRPC Kotlin (Coroutine 지원 핵심)
    implementation("io.grpc:grpc-kotlin-stub:${grpcKotlinVersion}")
    implementation("com.google.protobuf:protobuf-kotlin:${protobufVersion}")

    // Spring Boot 3.x (Jakarta EE) 환경에서 필요한 어노테이션
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
}

// 3. Protobuf 컴파일러 및 플러그인 설정
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        // Java용 gRPC 코드 생성 플러그인
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
        // Kotlin Coroutine용 gRPC 코드 생성 플러그인
        id("grpckotlin") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:${grpcKotlinVersion}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
                id("grpckotlin")
            }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("prepareKotlinBuildScriptModel") {}
