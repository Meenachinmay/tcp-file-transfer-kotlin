import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.protobuf") version "0.9.1"
    application
}

group = "org.meenachinmay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    // Grpc
    implementation("io.grpc:grpc-netty-shaded:1.54.0")
    implementation("io.grpc:grpc-protobuf:1.54.0")
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.22.2")
    implementation("net.devh:grpc-server-spring-boot-starter:2.14.0.RELEASE")

    testImplementation(kotlin("test"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.54.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}


tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    mainClass.set("org.meenachinmay.filetransfer.ServerAppKt")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    mainClass.set("org.meenachinmay.filetransfer.ClientAppKt")
    classpath = sourceSets["main"].runtimeClasspath
}

kotlin {
    jvmToolchain(21)
}