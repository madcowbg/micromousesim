package utils.geom

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private fun rotateHom2d(a: Double) = Mat3(Vec3(cos(a), sin(a), 0), Vec3(-sin(a), cos(a), 0), Vec3(0, 0, 1))

private fun scaleSepHom2d(s: Vec2) = Mat3(Vec3(s.x, 0, 0), Vec3(0, s.y, 0), Vec3(0, 0, 1))
private fun scaleHom2d(s: Double) = scaleSepHom2d(Vec2(s, s))
private fun translateHom2d(v: Vec2) = Mat3(Vec3(1, 0, 0), Vec3(0, 1, 0), Vec3(v.x, v.y, 1))

val Vec3.u: Vec2 get() = Vec2(x / z, y / z)
val Vec2.h: Vec3 get() = Vec3(x, y, 1)

typealias Pose = Mat3

infix fun Pose.ht(v: Vec2) = (this * v.h).u

val Number.rot: Pose get() = rotateHom2d(this.toDouble())
val Number.rotDeg: Pose get() = rotateHom2d(PI * this.toDouble() / 180)
val Vec2.transl: Pose get() = translateHom2d(this)

val Number.scale: Pose get() = scaleHom2d(this.toDouble())
val Vec2.scale: Pose get() = scaleSepHom2d(this)

infix fun Pose.then(second: Pose): Pose = second * this // transformations are applied left to right