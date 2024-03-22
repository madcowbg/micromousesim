package scene

import MouseSettings
import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import utils.geom.*
import kotlin.math.PI

private val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .4f)))

class Situation : Drawable {
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

    val size = labyrinth.size

    override fun draw(drawList: DrawList, drawPose: Mat3) {

        labyrinth.draw(drawList, drawPose)

        // rotate then translate (right to left)
        val mousePose = translateHom2d(Vec2(0.5, 0.5)) * rotateHom2d(PI * MouseSettings.orient / 180.0)

        mouse.draw(drawList, drawPose * mousePose)

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