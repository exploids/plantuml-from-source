plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
    kotlin("jvm") version "1.3.72"
}

group = "com.exploids"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.thoughtworks.qdox:qdox:2.0.0")
}

pluginBundle {
    website = "https://github.com/exploids/plantuml-from-source"
    vcsUrl = "https://github.com/exploids/plantuml-from-source"
    tags = listOf("plantuml", "uml", "diagram")
}

gradlePlugin {
    plugins {
        create("plantUmlFromSource") {
            id = "com.exploids.plantumlfromsource"
            displayName = "PlantUML from source"
            description = "A plugin that generates PlantUML class diagrams from java source files"
            implementationClass = "com.exploids.plantumlfromsource.gradle.PlantUmlPlugin"
        }
    }
}
