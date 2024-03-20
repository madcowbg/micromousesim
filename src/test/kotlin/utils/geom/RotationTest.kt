package utils.geom

import glm_.vec2.Vec2
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class RotationTest {

    @Test
    fun `rotating a to b applied on a produces b`() {
        val a = Vec2(1.2, 2.3).normalize()
        val b = Vec2(5.1, -2.3).normalize()

        val rotation = rot(a, b)

        assertTrue((rotation.times(a) - b).length() < SMALL_EPS, "${rotation.times(a)} != $b")
    }

    @Test
    fun `computing intersect distance`() {
        val p = Vec2(0, 0)
        val b = Vec2(1, 1)
        val q = Vec2(3, 0)
        val d = Vec2(0, 2)

        val intersect = intersection(p, b, q, d)!!
        assertEquals(1.2f, intersect.t)
        assertEquals(0.6f, intersect.u)

        val int1 = p + (b - p) * intersect.t // extrapolate to intersection
        val int2 = q + (d - q) * intersect.u // interpolate to intersection

        assertTrue((int1 - int2).length() < SMALL_EPS, "$int1 != $int2")
        assertTrue((int1 - intersect.point).length() < SMALL_EPS, "$int1 != $int2")
    }
}
