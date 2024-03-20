package utils.geom

import glm_.func.common.abs
import glm_.mat2x2.Mat2
import glm_.vec2.Vec2

const val SMALL_EPS = 1e-5

fun rot(from: Vec2, to: Vec2): Mat2 = Mat2(
    to.x * from.x + to.y * from.y, from.x * to.y - to.x * from.y,
    to.x * from.y - from.x * to.y, to.x * from.x + to.y * from.y
)

/**
 * Computes intersection of pb and qd.
 * @returns null if parallel segments, else Pair of distance of intersection along pb and of qd
 */
fun intersection(p: Vec2, b: Vec2, q: Vec2, d: Vec2): Intersection? {
    val r = b - p // direction 1
    val s = d - q // direction 2

    val rsCross = r.cross(s)
    return if (rsCross.abs < SMALL_EPS) {
        null // parallel lines
    } else {
        val t = (q - p).cross(s) / rsCross
        val u = (q - p).cross(r) / rsCross
        return Intersection(t, u, p + (b - p) * t)
    }
}

class Intersection(val t: Float, val u: Float, val point: Vec2)