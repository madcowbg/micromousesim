package scene

import imgui.classes.DrawList

class PlacedObject<O : Drawable>(val pose: Pose, val entity: O) : Drawable {
    override fun draw(drawList: DrawList, drawPose: Pose) = entity.draw(drawList, drawPose * pose)
}