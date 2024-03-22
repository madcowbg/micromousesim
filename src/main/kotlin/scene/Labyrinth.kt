package scene

import glm_.vec2.Vec2
import kotlin.math.abs

class Pt(val x: Int, val y: Int) {
    val vec: Vec2 get() = Vec2(x, y)
}

class Wall(val a: Pt, val b: Pt) {
    val lengthSq: Int get() = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
}

class Labyrinth(val size: Int, val walls: List<Wall>)

fun path(vararg pts: Pt): Array<Wall> {
    if (pts.size <= 1) return emptyArray()

    val res = mutableListOf<Wall>()
    var current = pts[0]
    for (i in 1 until pts.size) {
        // todo validate
        val wall = Wall(current, pts[i])
        check(wall.lengthSq == 1) { "wall length must be 1, but is ${wall.lengthSq}!" }
        current = pts[i]
        res += wall
    }
    return res.toTypedArray()
}

fun hvline(start: Pt, end: Pt): Array<Wall> {
    check(start.x == end.x || start.y == end.y)

    val length = abs(start.x - end.x) + abs(start.y - end.y)
    val xdir = (end.x - start.x) / length
    val ydir = (end.y - start.y) / length

    val pts: Array<Pt> = (0..length)
        .map { i -> Pt(start.x + xdir * i, start.y + ydir * i) }
        .toTypedArray()
    return path(*pts)
}

