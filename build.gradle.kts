plugins {
    kotlin("jvm") version "1.6.0" apply false
    id("org.jetbrains.dokka") version "1.5.30" apply false
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"

}

val g = "love.forte.di"
val v = "0.0.2"

group = g
version = v

repositories {
    mavenLocal()
    mavenCentral()
}

val credentialsUsernameKey = "sonatype.username"
val credentialsPasswordKey = "sonatype.password"
val secretKeyRingFileKey = "signing.secretKeyRingFile"

// set gpg file path to root
val secretKeyRingFile = extra.properties[secretKeyRingFileKey]!!.toString()
val secretRingFile = File(project.rootDir, secretKeyRingFile)
extra[secretKeyRingFileKey] = secretRingFile


subprojects {
    group = g
    version = v

    repositories {
        mavenLocal()
        mavenCentral()
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    configurePublishing(name)
    println("[publishing-configure] - [$name] configured.")
    setProperty(secretKeyRingFileKey, secretRingFile)

    signing {
        sign(publishing.publications)
    }

}




// nexus staging

val credentialsUsername: String = extra.properties[credentialsUsernameKey]!!.toString()
val credentialsPassword: String = extra.properties[credentialsPasswordKey]!!.toString()


nexusPublishing {
    packageGroup.set(P.GROUP)

    repositories {
        sonatype {
            username.set(credentialsUsername)
            password.set(credentialsPassword)
        }

    }
}

