plugins {
    kotlin("jvm") version "1.6.0" apply false
    id("org.jetbrains.dokka") version "1.5.30" apply false

}

group = "love.forte.di"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")


}


