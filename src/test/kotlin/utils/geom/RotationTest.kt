package utils.geom

import glm_.vec2.Vec2
import kotlin.test.Test
import kotlin.test.assertTrue

class RotationTest {

    @Test
    fun `rotating a to b applied on a produces b`() {
        val a = Vec2(1.2, 2.3).normalize()
        val b = Vec2(5.1, -2.3).normalize()

        val rotation = rot(a, b)

        assertTrue((rotation.times(a) - b).length() < 1e-7, "${rotation.times(a)} != $b")
    }

}