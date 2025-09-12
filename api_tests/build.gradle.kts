plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Rest-Assured
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("io.rest-assured:json-schema-validator:5.4.0")

    // Faker
    testImplementation("com.github.javafaker:javafaker:1.0.2")

    // Allure (sem plugin, só adapters)
    testImplementation("io.qameta.allure:allure-junit5:2.24.0")
    testImplementation("io.qameta.allure:allure-rest-assured:2.24.0")
}

tasks.test {
    useJUnitPlatform()

    // Direciona os resultados do Allure para dentro do build
    systemProperty("allure.results.directory", layout.buildDirectory.dir("allure-results").get().asFile.path)

    // (Opcional) mantém logs mais limpos no CI
    testLogging {
        events("FAILED", "SKIPPED")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

