package utils.geom

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.cos
import kotlin.math.sin

fun rotateHom2d(a: Double) = Mat3(Vec3(cos(a), sin(a), 0), Vec3(-sin(a), cos(a), 0), Vec3(0, 0, 1))
fun scaleSepHom2d(s: Vec2) = Mat3(Vec3(s.x, 0, 0), Vec3(0, s.y, 0), Vec3(0, 0, 1))
fun scaleHom2d(s: Double) = scaleSepHom2d(Vec2(s, s))
fun translateHom2d(v: Vec2) = Mat3(Vec3(1, 0, 0), Vec3(0, 1, 0), Vec3(v.x, v.y, 1))
val Vec3.u: Vec2 get() = Vec2(x / z, y / z)
val Vec2.h: Vec3 get() = Vec3(x, y, 1)
infix fun Mat3.ht(v: Vec2) = (this * v.h).u