package com.exploids.plantumlfromsource

import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMember

/**
 *
 * @author Luca Selinski
 */
enum class Visibility(val symbol: Char) {
    PUBLIC('+'),
    PROTECTED('#'),
    PACKAGE_PRIVATE('~'),
    PRIVATE('-');

    infix fun within(other: Visibility?): Boolean {
        return ordinal <= (other?.ordinal ?: -1)
    }

    override fun toString(): String {
        return symbol.toString()
    }
}

fun String.toVisibility(): Visibility {
    return when (this) {
        "public" -> Visibility.PUBLIC
        "protected" -> Visibility.PROTECTED
        "package private" -> Visibility.PACKAGE_PRIVATE
        "private" -> Visibility.PRIVATE
        else -> throw IllegalArgumentException("\"$this\" is not a valid value " +
                "(valid values are \"public\", \"protected\", \"package private\" and \"private\")")
    }
}

fun visibilityOf(member: JavaClass): Visibility {
    return when {
        member.isPublic -> Visibility.PUBLIC
        member.isProtected -> Visibility.PROTECTED
        member.isPrivate -> Visibility.PRIVATE
        else -> Visibility.PACKAGE_PRIVATE
    }
}


fun visibilityOf(member: JavaMember): Visibility {
    return when {
        member.isPublic -> Visibility.PUBLIC
        member.isProtected -> Visibility.PROTECTED
        member.isPrivate -> Visibility.PRIVATE
        else -> Visibility.PACKAGE_PRIVATE
    }
}
