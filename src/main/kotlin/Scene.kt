package examples

import examples.utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.dsl
import org.lwjgl.opengl.GL11
import kotlin.math.abs
import kotlin.math.min

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

fun path(vararg pts: Pt): Array<Wall> {
    if (pts.size <= 1) return emptyArray()

    val res = mutableListOf<Wall>()
    var current = pts[0]
    for (i in 1 until pts.size) {
        // todo validate
        val wall = Wall(current, pts[i])
        check(wall.lengthSq == 1) { "wall length must be 1, but is ${wall.lengthSq}!" }
        current = pts[i]
        res += wall
    }
    return res.toTypedArray()
}

fun hvline(start: Pt, end: Pt): Array<Wall> {
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
        *hvline(Pt(0, 1), Pt(2, 1)),
        Wall(Pt(3, 0), Pt(3, 1))
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

        drawLabyrinth()
    }

    private fun drawLabyrinth() {
        dsl.window("Labyrinth") {

            val OFFSET = 30
            var topLeftX: Float = ImGui.windowPos.x + OFFSET
            var topLeftY: Float = ImGui.windowPos.y + OFFSET

            var sizePerCell: Float =
                min(ImGui.windowWidth - 2 * OFFSET, ImGui.windowHeight - 2 * OFFSET) / labyrinth.size

            fun toMazeCoords(x: Number, y: Number) = Vec2(
                topLeftX + x.toFloat() * sizePerCell,
                topLeftY + y.toFloat() * sizePerCell
            )

            val drawList = ImGui.windowDrawList

            // maze background
            drawList.addRectFilled(
                toMazeCoords(0, 0),
                toMazeCoords(labyrinth.size, labyrinth.size),
                MAZE_BACKGROUND_COLOR
            )

            labyrinth.walls.forEach { wall ->
                drawList.addLine(
                    toMazeCoords(wall.a.x, wall.a.y),
                    toMazeCoords(wall.b.x, wall.b.y),
                    MAZE_WALL_COLOR,
                    2.0f
                )
            }

            for (x in 0 until labyrinth.size) {
                for (y in 0 until labyrinth.size) {
                    drawList.addText(
                        toMazeCoords(x + 0.4, y + 0.5),
                        MAZE_TEXT_COLOR,
                        "cell $x,$y"
                    )
                }
            }

            for (x in 0..labyrinth.size) {
                for (y in 0..labyrinth.size) {
                    drawList.addText(
                        toMazeCoords(x - 0.1, y - 0.1),
                        MAZE_TEXT_COLOR,
                        "($x,$y)"
                    )
                }
            }
        }
    }

}

class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"

}
