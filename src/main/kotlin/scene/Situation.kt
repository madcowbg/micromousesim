package scene

import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import imgui.classes.DrawList
import utils.geom.*
import kotlin.math.PI

interface Parameters {
    val mousePose: Pose
}

class StaticParameters(mousePos: Vec2, mouseRot: Float) : Parameters {
    override val mousePose: Pose = (PI / 2).rot then mouseRot.rotDeg then mousePos.transl
}

class Situation(parameters: Parameters) : Drawable {
    val labyrinth = Labyrinth(
        4,
        walls = listOf<Wall>()
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
        mouse = DynamicObject(
            parameters::mousePose,
            Mouse(
                0.5f,
                Sensors(
                    Laser(LASER_RED_COLOR),
                    Laser(LASER_GREEN_COLOR),
                    Laser(LASER_BLUE_COLOR)
                )
            )
        )
    )

    val size = labyrinth.size

    override fun draw(drawList: DrawList, drawPose: Mat3) {
        labyrinth.draw(drawList, drawPose)
    }
}
