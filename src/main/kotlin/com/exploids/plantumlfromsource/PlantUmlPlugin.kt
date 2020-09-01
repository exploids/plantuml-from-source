package com.exploids.plantumlfromsource

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * @author Luca Selinski
 */
open class PlantUmlPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val classDiagramExtension = project.extensions.create(
            "classDiagram",
            ClassDiagramExtension::class.java
        )
        project.tasks.create(
            "buildClassDiagram",
            BuildClassDiagramTask::class.java,
            classDiagramExtension
        )
    }
}
