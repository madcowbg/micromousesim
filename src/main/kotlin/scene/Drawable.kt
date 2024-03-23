package scene

import imgui.classes.DrawList
import utils.geom.Pose

interface Drawable {
    fun draw(drawList: DrawList, drawPose: Pose)
}