package scene

import MouseSettings
import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import imgui.classes.DrawList
import utils.geom.*
import kotlin.math.PI

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
        ),
        PlacedObject(
            // rotate then translate (right to left)
            translateHom2d(Vec2(0.5, 0.5)) * rotateHom2d(PI * MouseSettings.orient / 180.0),
            Mouse(0.5f) { mouse -> Laser(Vec2(0, 0), mouse.front) }
        )
    )

    val size = labyrinth.size

    override fun draw(drawList: DrawList, drawPose: Mat3) {
        labyrinth.draw(drawList, drawPose)
    }
}
