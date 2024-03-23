package scene

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import utils.geom.Intersection
import utils.geom.ht
import utils.geom.intersection
import kotlin.math.abs

class Pt(val x: Int, val y: Int) {
    val vec: Vec2 get() = Vec2(x, y)
}

class Wall(val a: Pt, val b: Pt) {
    val lengthSq: Int get() = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
}


private val MAZE_BACKGROUND_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.2f, 0.5f, 0f, 1f)))
private val MAZE_WALL_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 1f)))
private val MAZE_TEXT_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 0.5f)))

class Labyrinth(
    val size: Int,
    val walls: List<Wall>,
    val mouse: Mouse
) : Drawable {
    override fun draw(drawList: DrawList, drawPose: Mat3) {
        // maze background
        drawList.addRectFilled( // todo should be quad to support rotations!
            drawPose ht Vec2(0, 0),
            drawPose ht Vec2(this.size, this.size),
            MAZE_BACKGROUND_COLOR
        )

        this.walls.forEach { wall ->
            drawList.addLine(
                drawPose ht (wall.a.vec),
                drawPose ht (wall.b.vec),
                MAZE_WALL_COLOR,
                2.0f
            )
        }

        for (x in 0 until this.size) {
            for (y in 0 until this.size) {
                drawList.addText(
                    drawPose ht Vec2(x + 0.4, y + 0.5),
                    MAZE_TEXT_COLOR,
                    "cell $x,$y"
                )
            }
        }

        for (x in 0..this.size) {
            for (y in 0..this.size) {
                drawList.addText(
                    drawPose ht Vec2(x - 0.1, y - 0.1),
                    MAZE_TEXT_COLOR,
                    "($x,$y)"
                )
            }
        }

        mouse.draw(drawList, drawPose)

        intersections().forEach { (laser, intersects) ->
            intersects.forEach { intersect ->
                drawList.addCircleFilled(drawPose ht intersect.point, 5f, laser.plan.color)
            }
        }
    }

    fun intersections(): Map<Laser, List<Intersection>> =
        mouse.lasers.associateWith { laser -> laser.beam(mouse.poseInParent.get()).intersections(walls) }
}

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

