import utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.classes.DrawList
import imgui.dsl
import imgui.internal.sections.DrawFlag
import scene.*
import utils.geom.*
import kotlin.math.min
import kotlin.math.sqrt

object Scene {
    fun draw() {
        val situation = Situation(
            mousePos = MouseSettings.mousePos,
            mouseRot = MouseSettings.orient.toFloat()
        )
        if (UI.showMouse) {
            dsl.window("Mouse Plan") {
                val drawList = ImGui.windowDrawList
                val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

                val fitMouseToWindow =
                    mapFitRectToRect(
                        Vec2(1, 1) * -0.5,
                        Vec2(1, 1) * 0.5,
                        ImGui.windowPos,
                        ImGui.windowPos + drawSize
                    )

                situation.labyrinth.mouse.plan.draw(drawList, fitMouseToWindow)
            }
        }
        dsl.window("Labyrinth") {
            val offset = 30
            val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

            val drawList = ImGui.windowDrawList

            val mapLabyrinth = mapFitRectToRect(
                Vec2(0, situation.size),
                Vec2(situation.size, 0),
                ImGui.windowPos + offset,
                ImGui.windowPos + Vec2(drawSize, drawSize) - offset
            )

            situation.draw(drawList, mapLabyrinth)
        }

        dsl.window("Signal Strength vs Rotation") {
            val lines = situation.labyrinth.mouse.lasers.map { it.plan }.associateWith { mutableListOf<Vec2>() }
            for (orientation in -180..180) {
                val situation = Situation(
                    mousePos = MouseSettings.mousePos,
                    mouseRot = orientation.toFloat()
                )

                val distToFirst: Map<Laser, Float> =
                    situation.labyrinth.intersections().mapValues { (laser, intersections) ->
                        intersections.minOfOrNull { it.t } ?: 1000f // distance to intersection
                    }

                distToFirst.forEach { (laser, minDistance) -> lines[laser.plan]!!.add(Vec2(orientation, minDistance)) }
            }

            val topLeft = ImGui.windowPos
            val bottomRight = ImGui.windowPos + ImGui.windowSize

            drawLines(ImGui.windowDrawList, situation, topLeft, bottomRight, lines)
        }

        dsl.window("Signal Strength vs Movement") {
            
        }
    }
}

private fun drawLines(
    drawList: DrawList,
    situation: Situation,
    topLeft: Vec2,
    bottomRight: Vec2,
    lines: Map<LaserPlan, MutableList<Vec2>>
) {
    val mapToWindow = mapFitRectToRect(
        Vec2(-180, sqrt(2f) * situation.labyrinth.size),
        Vec2(180, 0),
        topLeft,
        bottomRight
    )

    lines.forEach { (laser, line) ->
        drawList.addPolyline(
            line.map { mapToWindow ht it },
            laser.color, DrawFlag.RoundCornersNone, thickness_ = 2f
        )
    }

    drawList.addRectFilled(
        mapToWindow ht Vec2(MouseSettings.orient.toFloat() - 5, 0),
        mapToWindow ht Vec2(MouseSettings.orient.toFloat() + 5, 10),
        ImGui.getColorU32(Vec4(arrayOf(0.4f, .4f, .4f, .6f)))
    )

    ImGui.text(situation.labyrinth.intersections().values.map { it.sortedBy { it.t }.first.t }.toString())
}


class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"
}
