import Situation.labyrinth
import Situation.mouse
import utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import imgui.dsl
import scene.*
import utils.geom.intersection
import utils.geom.rot
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


private val MAZE_BACKGROUND_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.2f, 0.5f, 0f, 1f)))
private val MAZE_WALL_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 1f)))
private val MAZE_TEXT_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 0.5f)))

private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

private val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .4f)))


class Position(val pos: Vec2, val orient: Vec2)

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

            Situation.draw(drawList, mapLabyrinth)

        }
    }
}


fun mapRot(from: Vec2, to: Vec2): (Vec2) -> Vec2 {
    val transf = rot(from, to)
    return { original -> transf.times(original) }
}

class Pose(val homTransf: Vec3)

interface Drawable {
    //fun draw(drawList: DrawList, pose: Pose) TODO switch
    fun draw(drawList: DrawList, mapLabyrinth: (original: Vec2) -> Vec2)
}

object Situation : Drawable {
    val labyrinth = Labyrinth(
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

    val mouse = Mouse(0.5f) // mouse looking to left

    override fun draw(drawList: DrawList, mapLabyrinth: (original: Vec2) -> Vec2) {

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
