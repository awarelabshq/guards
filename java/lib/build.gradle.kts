plugins {
    id("io.spring.dependency-management") version "1.1.0"
    id("maven-publish")
    kotlin("jvm") version "1.5.31"
}

group = "com.aware.guards"
version = "0.0.33"
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
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/awarelabshq/guards")
            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "aware-guards-java"
        }
    }
}