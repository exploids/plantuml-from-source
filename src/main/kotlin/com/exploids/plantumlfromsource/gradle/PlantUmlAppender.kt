package com.exploids.plantumlfromsource.gradle

import com.exploids.plantumlfromsource.gradle.model.Association
import com.exploids.plantumlfromsource.gradle.model.AssociationType
import com.thoughtworks.qdox.model.*

/**
 *
 * @author Luca Selinski
 */
class PlantUmlAppender(
        private val content: Appendable,
        private val knownClasses: Set<String>,
        private val colors: MutableMap<String, String>,
        private val minAccess: Visibility = Visibility.PUBLIC
) : Appendable {
    private var indentation = 0

    override fun append(csq: CharSequence): Appendable {
        content.append(csq)
        return this
    }

    override fun append(csq: CharSequence, start: Int, end: Int): Appendable {
        content.append(csq, start, end)
        return this
    }

    override fun append(c: Char): Appendable {
        content.append(c)
        return this
    }

    fun appendStart() {
        append("@startuml\n\n")
    }

    fun appendEnd() {
        append("\n@enduml\n")
    }

    fun appendPackage(pack: JavaPackage) {
        appendIndentation()
        append("package ")
        append(pack.name)
        if (pack.name in colors) {
            appendSpace()
            append(colors[pack.name])
        }
        appendOpenBrace()
        pack.classes.asSequence()
                .filter { isVisible(it) }
                .toSet()
                .forEach { entity ->
                    appendIndentation()
                    appendClassType(entity)
                    appendSpace()
                    append(entity.canonicalName)
                    val typeParameters = entity.getTypeParameters<JavaGenericDeclaration>()
                    if (typeParameters.isNotEmpty()) {
                        append("<")
                        typeParameters.joinTo(this, ", ") {
                            if (it.bounds == null || it.bounds.isEmpty()) {
                                it.name
                            } else {
                                val bounds = it.bounds.joinToString(" & ") { it.genericCanonicalName }
                                "${it.name} extends $bounds"
                            }
                        }
                        append(">")
                    }
//                    if (entity.superClass != null && entity.superClass.canonicalName in knownClasses) {
//                        append(" extends ")
//                        append(entity.superClass.canonicalName)
//                    }
//                    val implements = entity.implements.asSequence()
//                            .map { it.canonicalName }
//                            .filter { it in knownClasses }
//                            .toList()
//                    if (implements.isNotEmpty()) {
//                        append(" implements ")
//                        implements.asSequence().joinTo(this, ", ")
//                    }
                    if (entity.canonicalName in colors) {
                        appendSpace()
                        append(colors[entity.canonicalName])
                    }
                    if (entity.isSingleton) {
                        appendSpace()
                        append("<< (S,Khaki) Singleton >>")
                    }
                    appendOpenBrace()
                    entity.fields.asSequence()
                            .filter { isVisible(it) && (!entity.isEnum || it.isEnumConstant) }
                            .sortedBy { it.name }
                            .forEach { field ->
                                appendIndentation()
                                if (field.isEnumConstant) {
                                    append(field.name)
                                } else {
                                    appendModifiers(field)
                                    append(field.name)
                                    appendType(field.type)
                                }
                                appendNewLine()
                            }
                    appendIndentation()
                    append("--")
                    appendNewLine()
                    val constructors = entity.constructors.asSequence()
                            .filter { isVisible(it) && (!entity.isEnum || Visibility.PRIVATE within minAccess) }
                            .toSortedSet(ConstructorComparator)
                    constructors.forEach { constructor ->
                        appendIndentation()
                        if (entity.isEnum) {
                            appendVisibility(Visibility.PRIVATE)
                        } else {
                            appendModifiers(constructor)
                        }
                        append(entity.simpleName)
                        appendParameterList(constructor)
                        appendNewLine()
                    }
                    val accessors = entity.methods.asSequence()
                            .filter { isVisible(it) && (!entity.isEnum || !isEnumMethod(it)) && it.isAccessor }
                            .map { it.toAccessor() }
                            .toSortedSet()
                    if (constructors.isNotEmpty() && accessors.isNotEmpty()) {
                        appendIndentation()
                        append("..")
                        appendNewLine()
                    }
                    accessors.forEach { accessor ->
                        appendIndentation()
                        appendModifiers(accessor.method)
                        append(accessor.method.name)
                        appendParameterList(accessor.method)
                        appendType(accessor.method.returnType)
                        appendNewLine()
                    }
                    val methods = entity.methods.asSequence()
                            .filter { isVisible(it) && (!entity.isEnum || !isEnumMethod(it)) && !it.isAccessor && !isMethodAlreadyShown(it) }
                            .toSortedSet(MethodComparator)
                    if ((constructors.isNotEmpty() || accessors.isNotEmpty()) && methods.isNotEmpty()) {
                        appendIndentation()
                        append("..")
                        appendNewLine()
                    }
                    methods.forEach { method ->
                        appendIndentation()
                        appendModifiers(method)
                        append(method.name)
                        appendParameterList(method)
                        appendType(method.returnType)
                        appendNewLine()
                    }
                    appendCloseBrace()
                }
        for (subPackage in pack.subPackages) {
            appendPackage(subPackage)
        }
        appendCloseBrace()
    }

    fun appendAssociation(association: Association) {
        append(association.first.canonicalName)
        appendSpace()
        when (association.type) {
            AssociationType.USE -> {
                if (!association.firstCardinality.isNone) {
                    append("<")
                }
                append("..")
                if (!association.secondCardinality.isNone) {
                    append(">")
                }
            }
            AssociationType.COMPOSITION -> {
                if (!association.firstCardinality.isNone && association.firstCardinality.count != 1) {
                    appendEscaped(association.firstCardinality.toString())
                    appendSpace()
                }
                if (association.firstCardinality.isMany) {
                    append("o")
                } else if (!association.firstCardinality.isNone) {
                    append("*")
                }
                append("--")
                if (association.secondCardinality.isMany) {
                    append("o")
                } else if (!association.secondCardinality.isNone) {
                    append("*")
                }
                if (!association.secondCardinality.isNone && association.secondCardinality.count != 1) {
                    appendSpace()
                    appendEscaped(association.secondCardinality.toString())
                }
            }
            AssociationType.INHERITANCE -> append("--|>")
            AssociationType.IMPLEMENTATION -> append("..|>")
        }
        appendSpace()
        append(association.second.canonicalName)
        if (association.labels.isNotEmpty()) {
            append(" : ")
            association.labels.joinTo(this, ", ")
        }
        append('\n')
    }

    private fun appendEscaped(thing: String) {
        append('"').append(thing).append('"')
    }

    private fun appendSpace() {
        append(' ')
    }

    private fun appendNewLine() {
        append('\n')
    }

    private fun appendIndentation() {
        repeat(indentation) { append('\t') }
    }

    private fun appendOpenBrace() {
        append(" {\n")
        indentation += 1
    }

    private fun appendCloseBrace() {
        indentation -= 1
        appendIndentation()
        append("}\n\n")
    }

    private fun appendParameterList(executable: JavaExecutable) {
        executable.parameters.joinTo(this, ", ", "(", ")") { "${it.name}: ${it.genericValue}" }
    }

    private fun appendClassType(classe: JavaClass) {
        append(
                when {
                    classe.isEnum -> "enum"
                    classe.isInterface -> "interface"
                    classe.isAbstract -> "abstract class"
                    else -> "class"
                }
        )
    }

    private fun appendModifiers(member: JavaMember) {
        appendVisibility(visibilityOf(member))
        if (member.isStatic) {
            append("{static} ")
        }
    }

    private fun appendVisibility(visibility: Visibility) {
        append(visibility.symbol)
    }

    private fun appendType(classe: JavaType) {
        if (classe.toString() != "void") {
            append(": ")
            append(classe.genericValue)
        }
    }

    private fun isMethodAlreadyShown(method: JavaMethod): Boolean {
        var clazz = method.declaringClass.superJavaClass
        while (clazz != null) {
            if (isVisible(clazz) && clazz.canonicalName in knownClasses && clazz.getMethodBySignature(method.name, method.parameterTypes, false, method.isVarArgs) != null) {
                return true
            }
            clazz = clazz.superJavaClass
        }
        return false
    }

    private fun isVisible(member: JavaClass) = visibilityOf(member) within minAccess

    private fun isVisible(member: JavaMember) = visibilityOf(member) within minAccess

    private fun isEnumMethod(method: JavaMethod): Boolean {
        return (method.name == "values" && method.parameters.size == 0) || (method.name == "valueOf" && method.parameters.size == 1)
    }

    private val JavaClass.isSingleton: Boolean
        get() {
            val getInstance = getMethodBySignature("getInstance", emptyList())
            return if (getInstance != null && getInstance.isStatic && getInstance.returnType.canonicalName == canonicalName) {
                return constructors.isEmpty() || constructors.all { !it.isPublic }
            } else false
        }

    private val JavaMethod.isAccessor: Boolean
        get() = isPropertyAccessor || isPropertyMutator || isJavaFxPropertyAccessor

    private val JavaMethod.isJavaFxPropertyAccessor: Boolean
        get() {
            return !isStatic && parameters.isEmpty() && name.length > 8 && name.endsWith("Property")
        }

    private fun JavaMethod.toAccessor(): Accessor {
        return when {
            isPropertyAccessor -> Accessor(this, cutAndLower(name, if (name[0] == 'i') 2 else 3), 0)
            isPropertyMutator -> Accessor(this, cutAndLower(name, 3), 2)
            isJavaFxPropertyAccessor -> Accessor(this, name.substring(0, name.length - 8), 1)
            else -> throw IllegalArgumentException("$this is not an accessor")
        }
    }

    private fun cutAndLower(name: String, start: Int): String {
        return name[start].toLowerCase() + name.substring(start + 1)
    }

    private data class Accessor(
            val method: JavaMethod,
            val name: String,
            val type: Int
    ) : Comparable<Accessor> {
        override fun compareTo(other: Accessor): Int {
            return if (name == other.name) {
                type.compareTo(other.type)
            } else {
                name.compareTo(other.name)
            }
        }
    }

    private object ConstructorComparator : Comparator<JavaConstructor> {
        override fun compare(o1: JavaConstructor, o2: JavaConstructor): Int {
            return o1.parameters.size.compareTo(o2.parameters.size)
        }
    }

    private object MethodComparator : Comparator<JavaMethod> {
        override fun compare(o1: JavaMethod, o2: JavaMethod): Int {
            return if (o1.name == o2.name) {
                o1.parameters.size.compareTo(o2.parameters.size)
            } else {
                o1.name.compareTo(o2.name)
            }
        }
    }
}
