package examples

import imgui.ImGui
import imgui.dsl


object UI {
    var showMouseWindow: Boolean = true
    fun loop() {
        // 2. Show a simple window that we create ourselves. We use a Begin/End pair to create a named window.
        run {

            dsl.window("Diagnostics") {
                dsl.checkbox("Show Mouse", ::showMouseWindow) {}

                if (showMouseWindow) {
                    ImGui.text("TODO: show mouse window.")
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