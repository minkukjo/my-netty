import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "naver.minkuk"
version = "1.0"

repositories {
    mavenCentral()
}

val nettyVersion by extra("4.1.86.Final")

dependencies {
    implementation("io.netty:netty-all:$nettyVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
