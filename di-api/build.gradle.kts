plugins {
    kotlin("jvm") // version "1.6.0"
    id("org.jetbrains.dokka") // version "1.5.30"

}

group = "love.forte.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }


}

dependencies {
    api("javax.inject:javax.inject:1")
    compileOnly("org.springframework:spring-context:5.3.9") // component
    compileOnly("org.springframework:spring-core:5.3.9") // aliasFor
    compileOnly("org.springframework.boot:spring-boot:2.5.7") // ConfigurationProperties
}

kotlin {
    // 严格模式
    explicitApiWarning()


    sourceSets.all {
        languageSettings {
            optIn("kotlin.RequiresOptIn")
        }
    }
}

tasks.getByName<Test>("test") {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        javaParameters = true
        jvmTarget = "1.8"
    }
}
