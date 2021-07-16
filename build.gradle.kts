plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.12.0"
    kotlin("jvm") version "1.3.72"
}

group = "com.exploids"
version = "0.1.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.thoughtworks.qdox:qdox:2.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

pluginBundle {
    website = "https://github.com/exploids/plantuml-from-source"
    vcsUrl = "https://github.com/exploids/plantuml-from-source"
    description = "A plugin that generates PlantUML class diagrams from java source files"
    tags = listOf("plantuml", "uml", "diagram")

    plugins {
        create("plantUmlFromSource") {
            displayName = "PlantUML from source"
        }
    }
}

gradlePlugin {
    plugins {
        create("plantUmlFromSource") {
            id = "com.exploids.plantumlfromsource"
            implementationClass = "com.exploids.plantumlfromsource.gradle.PlantUmlPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
