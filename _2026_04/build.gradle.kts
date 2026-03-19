plugins {
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"

    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
}

group = "pro.developia"
version = "1.0.0"
description = "_2026_04"

// java toolchain 대신 사용
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    // 1. Spring WebFlux (Netty 기반 논블로킹 웹 & WebClient 포함)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 2. Spring Data R2DBC (논블로킹 DB 접근)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // 3. Kotlin & Coroutines (구조화된 동시성 및 리액티브 브릿지)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor") // WebFlux의 Mono/Flux와 Coroutine 호환

    // 4. Database Drivers
    // MySQL 8.x R2DBC 드라이버 (jasync-sql 기반)
    runtimeOnly("io.asyncer:r2dbc-mysql:1.1.3")
    // H2 R2DBC 드라이버 (통합 테스트용 인메모리 DB)
    testImplementation("io.r2dbc:r2dbc-h2")

    // 5. Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test") // WebTestClient 지원

    // Kotlin 전용 테스트 라이브러리
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test") // runTest 등 코루틴 테스트 지원
    testImplementation("io.mockk:mockk:1.13.10") // coEvery, coVerify 등 suspend 함수 모킹
    testImplementation("io.kotest:kotest-assertions-core:5.8.1") // shouldBe 등의 BDD 스타일 검증
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
