package scene

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import utils.geom.ht


private val MOUSE_BODY_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.1f, .2f, .8f, .9f)))
private val MOUSE_HEAD_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.4f, .2f, .2f, .9f)))

class Mouse(val size: Float) : Drawable {
    val front = Vec2(0, 1) // mouse face down

    override fun draw(drawList: DrawList, drawPose: Mat3) {
        val bodyA = Vec2(1, 1) * (-this.size / 2)
        val bodyB = Vec2(1, 1) * (+this.size / 2)

        drawList.addQuadFilled(
            drawPose ht bodyA,
            drawPose ht (Vec2(bodyA.x, bodyB.y)),
            drawPose ht (bodyB),
            drawPose ht (Vec2(bodyB.x, bodyA.y)),
            MOUSE_BODY_COLOR
        )

        drawList.addLine(
            drawPose ht (Vec2(0, 0)),
            drawPose ht (this.front * this.size * 0.4),
            MOUSE_HEAD_COLOR,
            thickness = 3f
        )
    }
}

class Laser(val orig: Vec2, val direction: Vec2) {
    fun beam(length: Number): Vec2 = orig + (direction - orig) * 1000
}
