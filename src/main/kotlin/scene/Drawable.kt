package scene

import glm_.mat3x3.Mat3
import imgui.classes.DrawList

interface Drawable {
    fun draw(drawList: DrawList, drawPose: Mat3)
}