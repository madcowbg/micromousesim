import utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import imgui.dsl
import utils.geom.intersection
import utils.geom.rot
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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

val labyrinth: Labyrinth = Labyrinth(
    4,
    listOf<Wall>()
            + hvline(Pt(0, 0), Pt(0, 4)) // left border
            + hvline(Pt(0, 4), Pt(4, 4))  // top border
            + hvline(Pt(4, 4), Pt(4, 0)) // right border
            + hvline(Pt(4, 0), Pt(0, 0)) // bottom border


            + listOf(
        *hvline(Pt(0, 1), Pt(2, 1)),
        *hvline(Pt(3, 0), Pt(3, 2)),
        *hvline(Pt(0, 2), Pt(1, 2)),
        *hvline(Pt(2, 1), Pt(2, 2)),
        *hvline(Pt(4, 3), Pt(1, 3))
    )
)


private val MAZE_BACKGROUND_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.2f, 0.5f, 0f, 1f)))
private val MAZE_WALL_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 1f)))
private val MAZE_TEXT_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 0.5f)))

private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

private val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .4f)))


class Position(val pos: Vec2, val orient: Vec2)
class Mouse(val size: Float) {
    val front = Vec2(0, 1) // mouse face down
}

val mouse = Mouse(0.5f) // mouse looking to left

class Laser(val orig: Vec2, val direction: Vec2)

object Scene {
    fun draw() {
        if (UI.showMouse) {
            dsl.window("Mouse") {
                val drawList = ImGui.windowDrawList
                val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

                val fitMouseToWindow =
                    mapFitRectToRect(
                        Vec2(1, 1) * -0.5,
                        Vec2(1, 1) * 0.5,
                        ImGui.windowPos,
                        ImGui.windowPos + drawSize
                    )

                drawMouse(mouse, drawList, fitMouseToWindow)
            }
        }

        dsl.window("Labyrinth") {
            val offset = 30
            val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

            val drawList = ImGui.windowDrawList

            val mapLabyrinth = mapFitRectToRect(
                Vec2(0, labyrinth.size),
                Vec2(labyrinth.size, 0),
                ImGui.windowPos + offset,
                ImGui.windowPos + Vec2(drawSize, drawSize) - offset
            )

            drawLabyrinth(labyrinth, drawList, mapLabyrinth)

            val mousePos = Position(
                Vec2(0.5, 0.5),
                Vec2(sin(MouseSettings.orient * Math.PI / 180), cos(MouseSettings.orient * Math.PI / 180))
            )

            val mouseRotation = mapRot(mousePos.orient, mouse.front)
            val g = { it: Vec2 -> mapLabyrinth(mousePos.pos + mouseRotation(it)) }

            drawMouse(mouse, drawList, g)

            val laser = Laser(mousePos.pos, mouseRotation(mouse.front))

            drawList.addLine(
                mapLabyrinth(laser.orig),
                mapLabyrinth(laser.orig + laser.direction * 1000),
                LASER_COLOR,
                thickness = 3f
            )

            for (wall in labyrinth.walls) {
                val intersect = intersection(laser.orig, laser.orig + laser.direction, wall.a.vec, wall.b.vec)
                if (intersect != null) {
                    if (intersect.t > 0 && intersect.u in 0.0..1.0) {
                        // we got an intersection!
                        drawList.addCircleFilled(mapLabyrinth(intersect.point), 5f, LASER_COLOR)
                    }
                }
            }
        }
    }
}


fun mapRot(from: Vec2, to: Vec2): (Vec2) -> Vec2 {
    val transf = rot(from, to)
    return { original -> transf.times(original) }
}

private fun drawMouse(mouse: Mouse, drawList: DrawList, g: (original: Vec2) -> Vec2) {
    val bodyA = Vec2(1, 1) * (-mouse.size / 2)
    val bodyB = Vec2(1, 1) * (+mouse.size / 2)

    drawList.addQuadFilled(
        g(bodyA), g(Vec2(bodyA.x, bodyB.y)), g(bodyB), g(Vec2(bodyB.x, bodyA.y)),
        MOUSE_BODY_COLOR
    )

    drawList.addLine(
        g(Vec2(0, 0)),
        g(mouse.front * mouse.size * 0.4),
        MOUSE_HEAD_COLOR,
        thickness = 3f
    )
}

private fun drawLabyrinth(labyrinth: Labyrinth, drawList: DrawList, map: (original: Vec2) -> Vec2) {
    // maze background
    drawList.addRectFilled(
        map(Vec2(0, 0)),
        map(Vec2(labyrinth.size, labyrinth.size)),
        MAZE_BACKGROUND_COLOR
    )

    labyrinth.walls.forEach { wall ->
        drawList.addLine(
            map(wall.a.vec),
            map(wall.b.vec),
            MAZE_WALL_COLOR,
            2.0f
        )
    }

    for (x in 0 until labyrinth.size) {
        for (y in 0 until labyrinth.size) {
            drawList.addText(
                map(Vec2(x + 0.4, y + 0.5)),
                MAZE_TEXT_COLOR,
                "cell $x,$y"
            )
        }
    }

    for (x in 0..labyrinth.size) {
        for (y in 0..labyrinth.size) {
            drawList.addText(
                map(Vec2(x - 0.1, y - 0.1)),
                MAZE_TEXT_COLOR,
                "($x,$y)"
            )
        }
    }
}

fun mapFitRectToRect(fromA: Vec2, fromB: Vec2, toA: Vec2, toB: Vec2): (original: Vec2) -> Vec2 = { original ->
    toA + (toB - toA) * (original - fromA) / (fromB - fromA)
}

class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"
}
