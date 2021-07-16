package com.exploids.plantumlfromsource.gradle

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class BuildClassDiagramTaskTest {

    @Test
    fun `single source directory in source set`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(JavaPlugin::class.java)

        val mainSourceSet = project.extensions
            .getByType(SourceSetContainer::class.java)
            .getByName("main")

        project.pluginManager.apply(PlantUmlPlugin::class.java)

        project.extensions.getByType(ClassDiagramExtension::class.java).sourceSet = mainSourceSet

        assertDoesNotThrow {
            project.tasks.withType(BuildClassDiagramTask::class.java).single().execute()
        }
    }

    @Test
    fun `multiple source directories in source set`() {
        val project = ProjectBuilder.builder().build()

        project.pluginManager.apply(JavaPlugin::class.java)

        val mainSourceSet = project.extensions
            .getByType(SourceSetContainer::class.java)
            .getByName("main")

        mainSourceSet.java.srcDir("src/custom/java")

        project.pluginManager.apply(PlantUmlPlugin::class.java)

        project.extensions.getByType(ClassDiagramExtension::class.java).sourceSet = mainSourceSet

        assertDoesNotThrow {
            project.tasks.withType(BuildClassDiagramTask::class.java).single().execute()
        }
    }
}