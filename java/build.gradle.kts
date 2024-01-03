plugins {
    id("io.spring.dependency-management") version "1.1.0"
    id("maven-publish")
    kotlin("jvm") version "1.5.31"
}

group = "com.aware.guards"
version = "0.0.35"
val springBootVersion = "2.7.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-aop:5.3.14") // Spring AOP
    implementation("org.aspectj:aspectjweaver:1.9.7") // Include AspectJ
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:1.29.0")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    implementation("io.opentelemetry:opentelemetry-api:1.33.0") // OpenTelemetry API
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:1.32.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8 // Set to the desired Java version
    targetCompatibility = JavaVersion.VERSION_1_8 // Set to the desired Java version
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId="com.aware.guards"
            artifactId = "aware-guards-java"
            version="0.0.35"
        }
    }
}