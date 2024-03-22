import Situation.labyrinth
import Situation.mouse
import glm_.mat3x3.Mat3
import utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import imgui.dsl
import scene.*
import utils.geom.Intersection
import utils.geom.intersection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


private val MAZE_BACKGROUND_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.2f, 0.5f, 0f, 1f)))
private val MAZE_WALL_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 1f)))
private val MAZE_TEXT_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 0.5f)))

private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

private val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .4f)))

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

interface Drawable {
    fun draw(drawList: DrawList, drawPose: Mat3)
}

fun rotateHom2d(a: Double) = Mat3(Vec3(cos(a), sin(a), 0), Vec3(-sin(a), cos(a), 0), Vec3(0, 0, 1))
fun scaleSepHom2d(s: Vec2) = Mat3(Vec3(s.x, 0, 0), Vec3(0, s.y, 0), Vec3(0, 0, 1))
fun scaleHom2d(s: Double) = scaleSepHom2d(Vec2(s, s))
fun translateHom2d(v: Vec2) = Mat3(Vec3(1, 0, 0), Vec3(0, 1, 0), Vec3(v.x, v.y, 1))
val Vec3.u: Vec2 get() = Vec2(x / z, y / z)
val Vec2.h: Vec3 get() = Vec3(x, y, 1)


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

    override fun draw(drawList: DrawList, drawPose: Mat3) {

        drawLabyrinth(labyrinth, drawList, drawPose)

        // rotate then translate (right to left)
        val mousePose = translateHom2d(Vec2(0.5, 0.5)) * rotateHom2d(PI * MouseSettings.orient / 180.0)

        drawMouse(mouse, drawList, drawPose * mousePose)

        val laser = Laser(Vec2(0, 0), mouse.front)

        drawList.addLine(
            drawPose ht (mousePose ht laser.orig),
            drawPose ht (mousePose ht laser.beam(1000)),// (tmp.orig + (tmp.direction - tmp.orig) * 1000)),
            LASER_COLOR,
            thickness = 3f
        )

        for (intersect in laser.intersections(mousePose)) {
            drawList.addCircleFilled(drawPose ht intersect.point, 5f, LASER_COLOR)
        }
    }

    private fun Laser.intersections(mouseInLabPose: Mat3): List<Intersection> {
        val laserOrigInLab = mouseInLabPose ht orig
        val laserBeamInLab = mouseInLabPose ht direction
        val result = mutableListOf<Intersection>()
        for (wall in labyrinth.walls) {
            val intersect = intersection(laserOrigInLab, laserBeamInLab, wall.a.vec, wall.b.vec)
            if (intersect != null) {
                if (intersect.t > 0 && intersect.u in 0.0..1.0) {
                    // we got an intersection!
                    result.add(intersect)
                }
            }
        }
        return result
    }
}

infix fun Mat3.ht(v: Vec2) = (this * v.h).u


private fun drawMouse(mouse: Mouse, drawList: DrawList, pose: Mat3) {
    val bodyA = Vec2(1, 1) * (-mouse.size / 2)
    val bodyB = Vec2(1, 1) * (+mouse.size / 2)

    drawList.addQuadFilled(
        pose ht bodyA, pose ht (Vec2(bodyA.x, bodyB.y)), pose ht (bodyB), pose ht (Vec2(bodyB.x, bodyA.y)),
        MOUSE_BODY_COLOR
    )

    drawList.addLine(
        pose ht (Vec2(0, 0)),
        pose ht (mouse.front * mouse.size * 0.4),
        MOUSE_HEAD_COLOR,
        thickness = 3f
    )
}

private fun drawLabyrinth(labyrinth: Labyrinth, drawList: DrawList, pose: Mat3) {
    // maze background
    drawList.addRectFilled( // todo should be quad to support rotations!
        pose ht Vec2(0, 0),
        pose ht Vec2(labyrinth.size, labyrinth.size),
        MAZE_BACKGROUND_COLOR
    )

    labyrinth.walls.forEach { wall ->
        drawList.addLine(
            pose ht (wall.a.vec),
            pose ht (wall.b.vec),
            MAZE_WALL_COLOR,
            2.0f
        )
    }

    for (x in 0 until labyrinth.size) {
        for (y in 0 until labyrinth.size) {
            drawList.addText(
                pose ht Vec2(x + 0.4, y + 0.5),
                MAZE_TEXT_COLOR,
                "cell $x,$y"
            )
        }
    }

    for (x in 0..labyrinth.size) {
        for (y in 0..labyrinth.size) {
            drawList.addText(
                pose ht Vec2(x - 0.1, y - 0.1),
                MAZE_TEXT_COLOR,
                "($x,$y)"
            )
        }
    }
}

fun mapFitRectToRect(fromA: Vec2, fromB: Vec2, toA: Vec2, toB: Vec2): Mat3 =
    //toA + (toB - toA) * (original - fromA) / (fromB - fromA)
    translateHom2d(toA) * scaleSepHom2d((toB - toA) / (fromB - fromA)) * translateHom2d(-fromA)

class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"
}
