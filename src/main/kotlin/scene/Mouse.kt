package scene

import glm_.vec2.Vec2

class Mouse(val size: Float) {
    val front = Vec2(0, 1) // mouse face down
}

class Laser(val orig: Vec2, val direction: Vec2) {
    fun beam(length: Number): Vec2 = orig + (direction - orig) * 1000
}
