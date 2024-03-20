package utils.geom

import glm_.mat2x2.Mat2
import glm_.vec2.Vec2

fun rot(from: Vec2, to: Vec2): Mat2 = Mat2(
    to.x * from.x + to.y * from.y, from.x * to.y - to.x * from.y,
    to.x * from.y - from.x * to.y, to.x * from.x + to.y * from.y
)