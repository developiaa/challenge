plugins {
    java
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "pro.developia"
version = "0.0.1-SNAPSHOT"
description = "_2026_03"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Artifact 이름 변경 (shardingsphere-jdbc-core -> shardingsphere-jdbc)
    // Maven Central에 없는 유령 의존성(test-util) 강제 제외
    implementation("org.apache.shardingsphere:shardingsphere-jdbc:5.5.0") {
        exclude(group = "org.apache.shardingsphere", module = "shardingsphere-test-util")
    }
    // ShardingSphere(Guava)의 전이 의존성 버전 누락 버그 해결
    implementation("com.google.j2objc:j2objc-annotations:2.8")

    // Java 11+ 환경에서 ShardingSphere 구동을 위한 JAXB 의존성
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.8")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("prepareKotlinBuildScriptModel") {}
