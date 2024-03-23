package scene

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import utils.geom.ht


private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

val FACE_DOWN_DIR = Vec2(0, 1)

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

    val front = FACE_DOWN_DIR // mouse face down
}

class Mouse(
    val plan: MousePlan,
    override val parent: Labyrinth,
    val poseInParent: Pose,
) : Drawable {
    val laser: Laser = Laser(plan.laser, this)

    override fun draw(drawList: DrawList, drawPose: Pose) {
        val pose = drawPose * poseInParent
        plan.draw(drawList, pose)

        laser.draw(drawList, pose)
    }
}

val LASER_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.7f, .1f, .7f, .3f)))


class LaserPlan(val orig: Vec2, val direction: Vec2 = FACE_DOWN_DIR /*TODO*/) {
    fun beam(length: Number): Vec2 = orig + (direction - orig) * 1000
}

class Laser(
    val plan: LaserPlan,
    override val parent: Mouse,
) : Drawable {

    override fun draw(drawList: DrawList, drawPose: Pose) {
        val pose = drawPose // todo * poseInParent

        drawList.addLine(
            pose ht plan.orig,
            pose ht plan.beam(1000),// (tmp.orig + (tmp.direction - tmp.orig) * 1000)),
            LASER_COLOR,
            thickness = 5f
        )
    }
}