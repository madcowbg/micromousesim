package examples

import examples.utils.settings.PersistedSettings
import examples.utils.settings.SimpleProps
import imgui.ImGui
import imgui.api.slider
import imgui.dsl

val simpleProps by lazy { SimpleProps("ui.props") }

object MouseSettings : PersistedSettings {
    override val settingsGroup: String = "ui.mouse"

    var topLeftX: Int by simpleProps.bind({ it.toInt() }, 100)
    var topLeftY: Int by simpleProps.bind({ it.toInt() }, 100)

    var width: Int by simpleProps.bind({ it.toInt() }, 200)
    var height: Int by simpleProps.bind({ it.toInt() }, 300)
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
                    ImGui.text("Coordinates:")
                    slider("X", MouseSettings::topLeftX, 0, ImGui.io.displaySize.x)
                    slider("Y", MouseSettings::topLeftY, 0, ImGui.io.displaySize.y)
                    slider("width", MouseSettings::width, 0, ImGui.io.displaySize.x)
                    slider("height", MouseSettings::height, 0, ImGui.io.displaySize.y)
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