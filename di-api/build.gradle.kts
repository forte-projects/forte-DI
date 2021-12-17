plugins {
    kotlin("jvm") // version "1.6.0"
    id("org.jetbrains.dokka") // version "1.5.30"
}

group = "love.forte.commons"
version = "1.0-SNAPSHOT"


dependencies {
    api("javax.inject:javax.inject:1")
    api("org.slf4j:slf4j-api:1.7.32")

    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
    compileOnly("org.springframework:spring-context:5.3.13") // component
    compileOnly("org.springframework:spring-core:5.3.13") // aliasFor
    compileOnly("org.springframework.boot:spring-boot:2.6.1") // ConfigurationProperties
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:2.6.1")
    compileOnly("love.forte.annotation-tool:api:0.6.1")
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
