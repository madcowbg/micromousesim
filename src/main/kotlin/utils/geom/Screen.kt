package utils.geom

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2

fun mapFitRectToRect(fromA: Vec2, fromB: Vec2, toA: Vec2, toB: Vec2): Mat3 =
    //toA + (toB - toA) * (original - fromA) / (fromB - fromA)
    translateHom2d(toA) * scaleSepHom2d((toB - toA) / (fromB - fromA)) * translateHom2d(-fromA)