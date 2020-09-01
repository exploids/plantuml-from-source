package com.exploids.plantumlfromsource.gradle

import com.exploids.plantumlfromsource.gradle.model.*
import com.thoughtworks.qdox.model.*

/**
 *
 * @author Luca Selinski
 */
class Associator(
    private val topLevelPackages: List<JavaPackage>,
    private val knownClasses: Set<String>,
    private val minAccess: Visibility? = Visibility.PUBLIC,
    private val minFieldAccess: Visibility? = Visibility.PRIVATE,
    private val minMethodAccess: Visibility? = null
) {
    private val hideSelfLink = true

    private val detectedAssociations = arrayListOf<Association>()

    fun associate(): List<Association> {
        topLevelPackages.asSequence()
            .flatMap { classesInPackage(it) }
            .filter { isVisible(it) }
            .toSet()
            .forEach { associateClass(it) }

//        detectedAssociations.sort()
        return detectedAssociations
    }

    private fun associateClass(type: JavaClass) {
        associateSuperClass(type)
        associateInterfaces(type)
        if (minAccess != null) {
            type.fields.asSequence()
                .filter { !it.isEnumConstant && isVisible(it) }
                .forEach { associateField(type, it) }
        }
        if (minMethodAccess != null) {
            type.methods.asSequence()
                .filter { isVisible(it) }
                .forEach { associateMethod(type, it) }
        }
        type.nestedClasses.asSequence()
            .filter { !isVisible(it) }
            .forEach { associateHiddenNestedClass(type, it) }
    }

    private fun associateSuperClass(type: JavaClass) {
        val superClass = type.superJavaClass
        if (superClass != null && isVisible(superClass) && superClass.canonicalName in knownClasses) {
            detectedAssociations += Association(type, superClass, AssociationType.INHERITANCE)
        }
    }

    private fun associateInterfaces(type: JavaClass) {
        findUniqueInterfaces(type).forEach {
            detectedAssociations += Association(type, it, AssociationType.IMPLEMENTATION)
        }
    }

    private fun findUniqueInterfaces(type: JavaClass): Set<JavaClass> {
        val implements = type.implements.asSequence()
            .filter { it is JavaClass && it.canonicalName in knownClasses }
            .map { it as JavaClass }
            .toSet()
        val superClass = type.superJavaClass
        return if (superClass == null) {
            implements
        } else {
            val superInterfaces = findUniqueInterfaces(superClass)
            if (isVisible(superClass) && superClass.canonicalName in knownClasses) {
                implements - superInterfaces
            } else {
                implements + superInterfaces
            }
        }
    }

    private fun associateHiddenNestedClass(type: JavaClass, nested: JavaClass) {
        if (minAccess != null) {
            nested.fields.asSequence()
                .filter { !it.isEnumConstant && isVisible(it) }
                .forEach { associateField(type, it) }
        }
        if (minMethodAccess != null) {
            nested.methods.asSequence()
                .filter { isVisible(it) }
                .forEach { associateMethod(type, it) }
        }
        nested.nestedClasses.asSequence()
            .filter { !isVisible(it) }
            .forEach { associateHiddenNestedClass(type, it) }
    }

    private fun associateField(type: JavaClass, field: JavaField) {
        getConcernedTypes(field).asSequence()
            .filter { canAppearsInDiagram(it) }
            .forEach { typeToLinkWith ->
                addOrUpdateAssociation(type, typeToLinkWith, FieldMember(field), isVisibleInDiagram(field))
            }
    }

    private fun associateMethod(type: JavaClass, method: JavaMethod) {
        method.parameters
            .forEach { parameter ->
                getConcernedTypes(parameter).asSequence()
                    .filter { canAppearsInDiagram(it) }
                    .forEach { typeToLinkWith ->
                        addOrUpdateAssociation(
                            type,
                            typeToLinkWith,
                            ParameterMember(parameter),
                            isVisibleInDiagram(method)
                        )
                    }
            }
        getConcernedTypes(method).asSequence()
            .filter { canAppearsInDiagram(it) }
            .forEach { typeToLinkWith ->
                addOrUpdateAssociation(type, typeToLinkWith, MethodMember(method), isVisibleInDiagram(method))
            }
    }

    private fun isVisible(type: JavaClass) = visibilityOf(type) within minAccess

    private fun isVisible(type: JavaMethod) = visibilityOf(type) within minMethodAccess

    private fun isVisible(type: JavaField) = visibilityOf(type) within minFieldAccess

    private fun isVisibleInDiagram(type: JavaMember) = visibilityOf(type) within minAccess

    private fun addOrUpdateAssociation(
        originClass: JavaClass,
        classToLinkWith: JavaClass,
        classMember: ClassMember,
        visible: Boolean
    ) {
        if (hideSelfLink && originClass == classToLinkWith) {
            return
        }
        val type = if (classMember is FieldMember) {
            AssociationType.COMPOSITION
        } else {
            AssociationType.USE
        }

        var existing = detectedAssociations.asSequence()
            .filter { it.concern(originClass, classToLinkWith, type) }
            .firstOrNull()
        val typeWithGeneric = classMember.type
        if (existing == null) {
            existing = Association(originClass, classToLinkWith, type)
            detectedAssociations += existing
        }
        if (existing.first != originClass) {
            existing.firstCardinality += cardinalityFor(typeWithGeneric)
        } else {
            existing.secondCardinality += cardinalityFor(typeWithGeneric)
        }
        if (visible && type == AssociationType.COMPOSITION) {
            existing.labels += classMember.name
        }
    }

    private fun cardinalityFor(type: JavaType): Cardinality {
        return if (isCollection(type)) MANY else ONE
    }

    private fun isCollection(classe: JavaType): Boolean {
        return classe is JavaClass && (classe.isArray || classe.isA("java.util.Collection"))
    }

    private fun canAppearsInDiagram(classe: JavaClass): Boolean {
        return knownClasses.contains(classe.canonicalName)
    }

    private fun getConcernedTypes(field: JavaField): Set<JavaClass> {
        val classes = mutableSetOf<JavaClass>()
        classes.add(field.type)
        classes.addAll(getGenericTypes(field.type))
        return classes
    }

    private fun getConcernedTypes(method: JavaMethod): Set<JavaClass> {
        val classes = mutableSetOf<JavaClass>()
        classes += method.returns
        classes.addAll(getGenericTypes(method.returnType))
        // manage parameters types
        for (parameter in method.parameters) {
            classes += getConcernedTypes(parameter)
        }
        return classes
    }

    private fun getConcernedTypes(parameter: JavaParameter): Set<JavaClass> {
        val classes = mutableSetOf<JavaClass>()
        classes += parameter.javaClass
        classes += getGenericTypes(parameter)
        return classes
    }

    private fun getGenericTypes(type: JavaParameterizedType): Set<JavaClass> {
        return type.actualTypeArguments.asSequence()
            .filter { it is JavaClass }
            .map { it as JavaClass }
            .toSet()
    }

    private fun getGenericTypes(type: JavaType): Set<JavaClass> {
        val classes = mutableSetOf<JavaClass>()
        classes += getArrayTypes(type)
        if (type is JavaParameterizedType) {
            classes += getGenericTypes(type)
        }
        return classes
    }

    private fun getArrayTypes(type: JavaType): Set<JavaClass> {
        return if (type is JavaClass && type.isArray) {
            setOf(type.componentType) + getArrayTypes(type.componentType)
        } else {
            emptySet()
        }
    }

    private fun classesInPackage(pack: JavaPackage): Sequence<JavaClass> {
        return sequenceOf(
            pack.classes.asSequence(),
            pack.subPackages.asSequence().flatMap { classesInPackage(it) }
        ).flatten()
    }

    interface ClassMember : Comparable<ClassMember> {
        val name: String
        val type: JavaType

        override fun compareTo(other: ClassMember): Int {
            return name.compareTo(other.name)
        }
    }

    class FieldMember(val field: JavaField) : ClassMember {
        override val name: String
            get() = this.field.name
        override val type: JavaType
            get() = this.field.type
    }

    class MethodMember(val method: JavaMethod) : ClassMember {
        override val name: String
            get() = method.name
        override val type: JavaType
            get() = method.returnType
    }

    class ParameterMember(val parameter: JavaParameter) : ClassMember {
        override val name: String
            get() = parameter.name
        override val type: JavaType
            get() = parameter.type
    }
}

