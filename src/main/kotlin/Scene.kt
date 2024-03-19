package examples

import examples.utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import org.lwjgl.opengl.GL11
import kotlin.math.abs

class Pt(val x: Int, val y: Int)
class Wall(val a: Pt, val b: Pt) {
    val lengthSq: Int get() = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
}

class Labyrinth(val size: Int, val walls: List<Wall>) {
//    class Builder(val size: Int) {
//        val walls = mutableListOf<Wall>()
//
//        operator fun plusAssign(wall: Wall) {
//            walls += wall
//        }
//    }

}

fun path(vararg pts: Pt): List<Wall> {
    if (pts.size <= 1) return emptyList()

    val res = mutableListOf<Wall>()
    var current = pts[0]
    for (i in 1 until pts.size) {
        // todo validate
        val wall = Wall(current, pts[i])
        check(wall.lengthSq == 1) { "wall length must be 1, but is ${wall.lengthSq}!" }
        current = pts[i]
        res += wall
    }
    return res
}

fun hvline(start: Pt, end: Pt): List<Wall> {
    check(start.x == end.x || start.y == end.y)

    val length = abs(start.x - end.x) + abs(start.y - end.y)
    val xdir = (end.x - start.x) / length
    val ydir = (end.y - start.y) / length

    val pts: Array<Pt> = (0..length)
        .map { i -> Pt(start.x + xdir * i, start.y + ydir * i) }
        .toTypedArray()
    return path(*pts)
}

val labyrinth: Labyrinth = Labyrinth(
    4,
    listOf<Wall>()
            + hvline(Pt(0, 0), Pt(0, 4)) // left border
            + hvline(Pt(0, 4), Pt(4, 4))  // top border
            + hvline(Pt(4, 4), Pt(4, 0)) // right border
            + hvline(Pt(4, 0), Pt(0, 0)) // bottom border
            + listOf(
        Wall(Pt(1, 1), Pt(2, 1))
    )
)


private val MAZE_BACKGROUND_COLOR = ImGui.getColorU32(Vec4(arrayOf(0.2f, 0.5f, 0f, 1f)))
private val MAZE_WALL_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 1f)))
private val MAZE_TEXT_COLOR = ImGui.getColorU32(Vec4(arrayOf(1f, 1f, 1f, 0.5f)))


object Scene {
    fun draw() {

        //ImGui.backgroundDrawList.addRectFilled(Vec2(20, 30), Vec2(100, 200), ImGui.getColorU32(40, 150, 70, 255))

        val drawList = ImGui.backgroundDrawList
        if (UI.showMouse) {
            drawList.addRectFilled(
                Vec2(
                    MouseSettings.topLeftX,
                    MouseSettings.topLeftY,
                ), Vec2(
                    MouseSettings.topLeftX + MouseSettings.width,
                    MouseSettings.topLeftY + MouseSettings.height
                ), ImGui.getColorU32(50, 100, 90, 255)
            )
        }

        // maze background
        drawList.addRectFilled(
            MazeSettings.toMazeCoords(0, 0),
            MazeSettings.toMazeCoords(labyrinth.size, labyrinth.size),
            MAZE_BACKGROUND_COLOR
        )

        labyrinth.walls.forEach { wall ->
            drawList.addLine(
                MazeSettings.toMazeCoords(wall.a.x, wall.a.y),
                MazeSettings.toMazeCoords(wall.b.x, wall.b.y),
                MAZE_WALL_COLOR,
                2.0f
            )
        }

        for (x in 0 until labyrinth.size) {
            for (y in 0 until labyrinth.size) {
                drawList.addText(
                    MazeSettings.toMazeCoords(x + 0.4, y + 0.5),
                    MAZE_TEXT_COLOR,
                    "cell $x,$y"
                )
            }
        }

        for (x in 0..labyrinth.size) {
            for (y in 0..labyrinth.size) {
                drawList.addText(
                    MazeSettings.toMazeCoords(x - 0.1, y - 0.1),
                    MAZE_TEXT_COLOR,
                    "($x,$y)"
                )
            }
        }


    }
}

object MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"

    var topLeftX: Int by simpleProps.bind({ it.toInt() }, 200)
    var topLeftY: Int by simpleProps.bind({ it.toInt() }, 200)

    var sizePerCell: Int by simpleProps.bind({ it.toInt() }, 100)

    fun toMazeCoords(x: Number, y: Number) = Vec2(
        topLeftX + x.toFloat() * sizePerCell,
        topLeftY + y.toFloat() * sizePerCell
    )
}
