package com.exploids.plantumlfromsource.gradle.model

/**
 *
 * @author Luca Selinski
 */
class Cardinality(val count: Int) {
    val isMany: Boolean
        get() = count == Int.MAX_VALUE

    val isNone: Boolean
        get() = count == 0

    operator fun plus(other: Cardinality): Cardinality {
        return when {
            isMany -> this
            other.isMany -> other
            else -> Cardinality(this.count + other.count)
        }
    }

    override fun toString(): String {
        return when {
            isNone -> ""
            isMany -> "*"
            else -> count.toString()
        }
    }
}

val MANY = Cardinality(Int.MAX_VALUE)
val ONE = Cardinality(1)
val NONE = Cardinality(0)
