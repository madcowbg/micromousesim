package scene

import imgui.classes.DrawList

interface Drawable {
    fun draw(drawList: DrawList, drawPose: Pose)
    val parent: Drawable?
}