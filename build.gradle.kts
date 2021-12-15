plugins {
    kotlin("jvm") version "1.6.0" apply false
    id("org.jetbrains.dokka") version "1.5.30" apply false

}

val g = "love.forte.di"
val v = "0.0.1"

group = g
version = v

repositories {
    mavenLocal()
    mavenCentral()
}

subprojects {
    group = g
    version = v

    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")


}


