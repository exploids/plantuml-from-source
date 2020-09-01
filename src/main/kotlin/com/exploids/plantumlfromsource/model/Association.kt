package com.exploids.plantumlfromsource.model

import com.thoughtworks.qdox.model.JavaClass

/**
 *
 * @author Luca Selinski
 */
class Association(
        val first: JavaClass,
        val second: JavaClass,
        val type: AssociationType,
        var firstCardinality: Cardinality = NONE,
        var secondCardinality: Cardinality = NONE,
        val labels: MutableList<String> = mutableListOf()
) {
    fun concern(one: JavaClass, another: JavaClass, type: AssociationType): Boolean {
        return ((one == first && another == second) || (one == second && another == first)) && type == this.type
    }
}
