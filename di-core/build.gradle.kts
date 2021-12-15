plugins {
    kotlin("jvm") // version "1.6.0"
    id("org.jetbrains.dokka") // version "1.5.30"

}

group = "love.forte.commons"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(project(":di-api"))
    api(kotlin("reflect", version = "1.6.0"))
    compileOnly("org.springframework:spring-context:5.3.13") // component
    compileOnly("org.springframework:spring-core:5.3.13") // aliasFor
    compileOnly("org.springframework.boot:spring-boot:2.6.1") // ConfigurationProperties

    testImplementation("javax.inject:javax.inject:1")
    testImplementation("love.forte.annotation-tool:kcore:0.6.1")
    testImplementation("org.springframework:spring-context:5.3.13") // component
    testImplementation("org.springframework:spring-core:5.3.13") // aliasFor
    testImplementation("org.springframework.boot:spring-boot:2.6.1") // ConfigurationProperties
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
