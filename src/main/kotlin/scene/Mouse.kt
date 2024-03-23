package scene

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import utils.geom.Intersection
import utils.geom.ht
import utils.geom.intersection
import utils.geom.rotateHom2d
import kotlin.math.PI


private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

class MousePlan(val size: Float, val laser: LaserPlan) {
    fun draw(drawList: DrawList, drawPose: Pose) {

        val bodyA = Vec2(1, 1) * (-size / 2)
        val bodyB = Vec2(1, 1) * (+size / 2)

        drawList.addQuadFilled(
            drawPose ht bodyA,
            drawPose ht (Vec2(bodyA.x, bodyB.y)),
            drawPose ht (bodyB),
            drawPose ht (Vec2(bodyB.x, bodyA.y)),
            MOUSE_BODY_COLOR
        )

        drawList.addLine(
            drawPose ht (Vec2(0, 0)),
            drawPose ht (front * size * 0.4),
            MOUSE_HEAD_COLOR,
            thickness = 3f
        )

    }

    val front = Vec2(1, 0) // mouse face down
}

class Mouse(
    val plan: MousePlan,
    override val parent: Labyrinth,
    val poseInParent: Pose,
) : Drawable {
    val laser: Laser = Laser(plan.laser, this, Mat3.identity)

    override fun draw(drawList: DrawList, drawPose: Pose) {
        val pose = drawPose * poseInParent
        plan.draw(drawList, pose)

        laser.draw(drawList, pose)
    }
}

val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .3f)))


class LaserPlan {
    fun draw(drawList: DrawList, drawPose: Pose) {
        drawList.addLine(
            drawPose ht Vec2(0, 0),
            drawPose ht Vec2(1000, 0),// (tmp.orig + (tmp.direction - tmp.orig) * 1000)),
            LASER_COLOR,
            thickness = 5f
        )
    }
}

class Beam(pose: Pose) {
    val orig: Vec2 = pose ht Vec2(0, 0)
    val direction: Vec2 = pose ht Vec2(1, 0)

    fun intersections(walls: List<Wall>): List<Intersection> {
        val result = mutableListOf<Intersection>()
        for (wall in walls) {
            val intersect = intersection(this.orig, this.direction, wall.a.vec, wall.b.vec)
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

class Laser(
    val plan: LaserPlan,
    override val parent: Mouse,
    val poseInParent: Pose
) : Drawable {
    override fun draw(drawList: DrawList, drawPose: Pose) {
        val pose = drawPose * poseInParent
        plan.draw(drawList, pose)
    }

    fun beam(parentPose: Pose): Beam = Beam(parentPose * poseInParent)
}