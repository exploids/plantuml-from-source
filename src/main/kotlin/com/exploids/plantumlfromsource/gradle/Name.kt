package com.exploids.plantumlfromsource.gradle

/**
 * A dot-separated name.
 *
 * @author Luca Selinski
 */
class Name(val parts: List<String>) {
    fun isChildOf(other: Name): Boolean {
        if (parts.size < other.parts.size) {
            return false
        } else {
            for (index in other.parts.indices) {
                if (parts[index] != other.parts[index]) {
                    return false
                }
            }
            return true
        }
    }
}

fun nameOf(text: String): Name {
    val parts = text.split('.')
    return Name(parts)
}
