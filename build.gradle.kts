plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.72"
}

group = "com.exploids"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.thoughtworks.qdox:qdox:2.0.0")
}

gradlePlugin {
    plugins {
        create("plantumlFromSource") {
            id = "com.exploids.plantumlfromsource"
            implementationClass = "com.exploids.plantumlfromsource.PlantUmlPlugin"
        }
    }
}
