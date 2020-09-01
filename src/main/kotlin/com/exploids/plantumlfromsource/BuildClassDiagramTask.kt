package com.exploids.plantumlfromsource

import com.thoughtworks.qdox.JavaProjectBuilder
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaPackage
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 *
 * @author Luca Selinski
 */
open class BuildClassDiagramTask @Inject constructor(
        private val extension: ClassDiagramExtension
) : DefaultTask() {

    init {
        group = "plantuml"
        description = "Build a PlantUML class diagram."
    }

    @TaskAction
    fun execute() {
        val sourceSet = extension.sourceSet ?: return logger.debug("no source set")
        val output = extension.outputFile ?: project.buildDir.resolve("plantuml")
        logger.debug("source: {}, output: {}", sourceSet, output)

        val visibility = extension.visibility.toVisibility()
        val associationFieldVisibility = extension.associationFieldVisibility?.toVisibility()
        val associationMethodVisibility = extension.associationMethodVisibility?.toVisibility()

        val builder = JavaProjectBuilder()
        builder.addSourceTree(sourceSet.allJava.sourceDirectories.singleFile)

        val topLevelPackages = builder.packages.asSequence()
                .filter { pack ->
                    val firstName = nameOf(pack.name)
                    builder.packages.asSequence().all { !firstName.isChildOf(nameOf(it.name)) }
                }
                .toList()

        val knownClasses = topLevelPackages.asSequence()
                .flatMap { classesInPackage(it) }
                .filter { visibilityOf(it) within visibility }
                .map { it.canonicalName }
                .toSet()

        val associations = Associator(
                topLevelPackages,
                knownClasses,
                visibility,
                associationFieldVisibility,
                associationMethodVisibility
        ).associate()

        output.parentFile.mkdirs()
        output.bufferedWriter().use { writer ->
            PlantUmlAppender(writer, knownClasses, extension.colors, visibility).apply {
                appendStart()
                if (extension.before.isNotBlank()) {
                    append(extension.before)
                    append("\n")
                }
                topLevelPackages.forEach { appendPackage(it) }
                associations.forEach { appendAssociation(it) }
                appendEnd()
            }
        }
    }

    private fun classesInPackage(pack: JavaPackage): Sequence<JavaClass> {
        return sequenceOf(
                pack.classes.asSequence(),
                pack.subPackages.asSequence().flatMap { classesInPackage(it) }
        ).flatten()
    }
}
