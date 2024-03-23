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
        val currentSituation = Situation(
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

                currentSituation.labyrinth.mouse.plan.draw(drawList, fitMouseToWindow)
            }
        }
        dsl.window("Labyrinth") {
            val offset = 30
            val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

            val drawList = ImGui.windowDrawList

            val mapLabyrinth = mapFitRectToRect(
                Vec2(0, currentSituation.size),
                Vec2(currentSituation.size, 0),
                ImGui.windowPos + offset,
                ImGui.windowPos + Vec2(drawSize, drawSize) - offset
            )

            currentSituation.draw(drawList, mapLabyrinth)
        }

        dsl.window("Signal Strength vs Rotation") {
            val lines = currentSituation.labyrinth.mouse.lasers.map { it.plan }.associateWith { mutableListOf<Vec2>() }
            for (orientation in -180..180) {
                val situation = Situation(
                    mousePos = MouseSettings.mousePos,
                    mouseRot = orientation.toFloat()
                )

                distToFirst(situation)
                    .forEach { (laser, minDistance) ->
                        lines[laser.plan]?.add(Vec2(orientation, minDistance))
                    }
            }

            val topLeft = ImGui.windowPos
            val bottomRight = ImGui.windowPos + ImGui.windowSize

            val mapToWindow = mapFitRectToRect(
                Vec2(-180, sqrt(2f) * currentSituation.labyrinth.size),
                Vec2(180, 0),
                topLeft,
                bottomRight
            )

            drawLines(
                ImGui.windowDrawList,
                mapToWindow,
                Pair(MouseSettings.orient.toFloat() - 3, MouseSettings.orient.toFloat() + 3),
                lines
            )

            ImGui.text(currentSituation.labyrinth.intersections().values.map { it.sortedBy { it.t }.first.t }
                .toString())
        }

        dsl.window("Signal Strength vs Movement") {
            val lines = currentSituation.labyrinth.mouse.lasers.map { it.plan }.associateWith { mutableListOf<Vec2>() }
            for (positionX in 20..280) {
                val situation = Situation(
                    mousePos = Vec2(positionX / 100f, 0.5),
                    mouseRot = MouseSettings.orient.toFloat()
                )

                distToFirst(situation)
                    .forEach { (laser, minDistance) ->
                        lines[laser.plan]?.add(Vec2(positionX, minDistance))
                    }
            }

            val topLeft = ImGui.windowPos
            val bottomRight = ImGui.windowPos + ImGui.windowSize

            val mapToWindow = mapFitRectToRect(
                Vec2(20, sqrt(2f) * currentSituation.labyrinth.size),
                Vec2(280, 0),
                topLeft,
                bottomRight
            )

            drawLines(
                ImGui.windowDrawList,
                mapToWindow,
                Pair(MouseSettings.pos.toFloat() * 10 - 4, MouseSettings.pos.toFloat() * 10 + 4),
                lines
            )
        }
    }

    private fun distToFirst(situation: Situation): Map<Laser, Float> =
        situation.labyrinth.intersections().mapValues { (laser, intersections) ->
            intersections.minOfOrNull { it.t } ?: 1000f // distance to intersection
        }
}

private fun drawLines(
    drawList: DrawList,
    mapToWindow: Pose,
    current: Pair<Float, Float>,
    lines: Map<LaserPlan, MutableList<Vec2>>
) {
    lines.forEach { (laser, line) ->
        drawList.addPolyline(
            line.map { mapToWindow ht it },
            laser.color, DrawFlag.RoundCornersNone, thickness_ = 2f
        )
    }

    drawList.addRectFilled(
        mapToWindow ht Vec2(current.first, 0),
        mapToWindow ht Vec2(current.second, 10),
        ImGui.getColorU32(Vec4(arrayOf(0.4f, .4f, .4f, .6f)))
    )

}


class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"
}
