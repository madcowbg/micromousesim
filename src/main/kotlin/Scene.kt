import scene.Situation.labyrinth
import scene.Situation.mouse
import utils.settings.PersistedSettings
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.ImGui
import imgui.dsl
import scene.*
import utils.geom.*
import kotlin.math.min


object Scene {
    fun draw() {
        if (UI.showMouse) {
            dsl.window("Mouse") {
                val drawList = ImGui.windowDrawList
                val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

                val fitMouseToWindow =
                    mapFitRectToRect(
                        Vec2(1, 1) * -0.5,
                        Vec2(1, 1) * 0.5,
                        ImGui.windowPos,
                        ImGui.windowPos + drawSize
                    )

                mouse.draw(drawList, fitMouseToWindow)
            }
        }

        dsl.window("Labyrinth") {
            val offset = 30
            val drawSize = min(ImGui.windowWidth, ImGui.windowHeight)

            val drawList = ImGui.windowDrawList

            val mapLabyrinth = mapFitRectToRect(
                Vec2(0, labyrinth.size),
                Vec2(labyrinth.size, 0),
                ImGui.windowPos + offset,
                ImGui.windowPos + Vec2(drawSize, drawSize) - offset
            )

            Situation.draw(drawList, mapLabyrinth)

        }
    }
}


class MazeSettings : PersistedSettings {
    override val settingsGroup: String = "ui.maze"
}
