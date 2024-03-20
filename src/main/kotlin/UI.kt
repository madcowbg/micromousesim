import utils.settings.PersistedSettings
import utils.settings.SimpleProps
import imgui.ImGui
import imgui.api.slider
import imgui.dsl
import kotlin.math.PI

val simpleProps by lazy { SimpleProps("ui.props") }

object MouseSettings : PersistedSettings {
    override val settingsGroup: String = "ui.mouse"

    var orient: Int by simpleProps.bind({ it.toInt() }, -90)
}

object UI {
    var showMouse: Boolean = true
    var showDemoWindow: Boolean = false

    fun loop() {
        run {
            if (showDemoWindow)
                ImGui.showDemoWindow(::showDemoWindow)

            dsl.window("Settings") {
                dsl.checkbox("Show Mouse", ::showMouse) {}
                if (showMouse) {
                    slider("phi", MouseSettings::orient, 180, -180)
                    ImGui.text("Coordinates:")
                }

                dsl.checkbox("Show Demo Window", ::showDemoWindow) {}
            }

            dsl.window("Diagnostics") {
                //dsl.button("Save UI") {}
                ImGui.text("Settings filename: ${ImGui.io.iniFilename}")

                if (showMouse) {
                    ImGui.text("Showing mouse...")
                }

                ImGui.text(
                    "Application average %.3f ms/frame (%.1f FPS)",
                    1_000f / ImGui.io.framerate,
                    ImGui.io.framerate
                )
            }


        }
    }
}