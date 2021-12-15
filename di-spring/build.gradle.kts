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
    api(project(":di-api"))
    api(kotlin("reflect", version = "1.6.0"))

    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    annotationProcessor("com.google.auto.service:auto-service:1.0.1")

    implementation("org.springframework:spring-context:5.3.13") // component
    testImplementation("org.springframework.boot:spring-boot-starter-web:2.6.1")
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
