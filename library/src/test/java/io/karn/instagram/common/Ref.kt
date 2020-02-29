package io.karn.instagram.common

import org.junit.Test
import kotlin.test.assertEquals

class Ref {

    class Foo {
        var str = "hello"
    }

    class Bar(val bar: Foo) {

        fun transform() {
            bar.str = "world"
        }
    }

    @Test
    fun testRef() {
        val f = Foo()

        assertEquals("hello", f.str)

        val b = Bar(f)
        b.transform()

        assertEquals("world", f.str)

        f.str = "baz"

        assertEquals("baz", b.bar.str)
    }
}
