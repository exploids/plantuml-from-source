package com.exploids.plantumlfromsource.gradle

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NameTest {
    @Test
    fun `nameOf regular`() {
        Assertions.assertEquals(nameOf("aaa.b.cc"), Name(listOf("aaa", "b", "cc")))
    }

    @Test
    fun `nameOf single`() {
        Assertions.assertEquals(nameOf("a"), Name(listOf("a")))
    }

    @Test
    fun `name should be a child of the other name`() {
        Assertions.assertTrue(nameOf("a.b.c").isChildOf(nameOf("a.b")))
    }

    @Test
    fun `parent should not be a child of its child`() {
        Assertions.assertFalse(nameOf("a.b").isChildOf(nameOf("a.b.c")))
    }

    @Test
    fun `same names should not be children of each other`() {
        Assertions.assertFalse(nameOf("a.b.c").isChildOf(nameOf("a.b.c")))
    }

    @Test
    fun `siblings should not be children of each other`() {
        Assertions.assertFalse(nameOf("a.b.c").isChildOf(nameOf("a.b.d")))
    }
}
