plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
    kotlin("jvm") version "1.3.72"
}

group = "com.exploids"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.thoughtworks.qdox:qdox:2.0.0")
}

gradlePlugin {
    plugins {
        create("plantUmlFromSource") {
            id = "com.exploids.plantumlfromsource"
            implementationClass = "com.exploids.plantumlfromsource.gradle.PlantUmlPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/exploids/plantuml-from-source"
    vcsUrl = "https://github.com/exploids/plantuml-from-source"
    tags = listOf("plantuml", "uml", "diagram")
}
