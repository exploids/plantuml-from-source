package com.exploids.plantumlfromsource

import org.gradle.api.tasks.SourceSet
import java.io.File

/**
 *
 * @author Luca Selinski
 */
open class ClassDiagramExtension {
    var before: String = "skinparam shadowing false"
    var sourceSet: SourceSet? = null
    var outputFile: File? = null
    var ignoreClasses: MutableList<String> = mutableListOf()
    var colors: MutableMap<String, String> = mutableMapOf()
    var visibility: String = "public"
    var associationFieldVisibility: String? = "private"
    var associationMethodVisibility: String? = "public"
}
