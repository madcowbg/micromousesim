import glm_.vec2.Vec2
import utils.settings.PersistedSettings
import utils.settings.SimpleProps
import imgui.ImGui
import imgui.api.slider
import imgui.dsl

val simpleProps by lazy { SimpleProps("ui.props") }

object MouseSettings : PersistedSettings {

    override val settingsGroup: String = "ui.mouse"

    var orient: Int by simpleProps.bind({ it.toInt() }, -90)
    var pos: Int by simpleProps.bind({ it.toInt() }, 5)

    val mousePos: Vec2 get() = Vec2(pos / 10.0, 0.5)
}

object UI {
    var showMouse: Boolean = true
    var showDemoWindow: Boolean = false

    fun loop() {
        timeExec("All") {
            SceneUI.draw() // custom code

            if (showDemoWindow)
                ImGui.showDemoWindow(::showDemoWindow)

            dsl.window("Settings") {
                dsl.checkbox("Show Mouse", ::showMouse) {}

                slider("phi", MouseSettings::orient, 180, -180)
                slider("pos", MouseSettings::pos, 2, 28)

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

                ImGui.text("Average Timing:")
                timingsNano.entries.sortedBy { it.key }
                    .forEach { (key, value) -> ImGui.text("  $key: ${value / 1000000f} ms") }
            }
        }
    }
}

private val timingsNano = mutableMapOf<String, Long>()
private const val RECENT_WT = 0.04
fun weight(new: Long, old: Long): Long = (new * RECENT_WT + old * (1 - RECENT_WT)).toLong()

fun timeExec(token: String, f: () -> Unit) {
    val start = System.nanoTime()
    try {
        f()
    } finally {
        timingsNano[token] = weight(System.nanoTime() - start, timingsNano.getOrDefault(token, 0))
    }
}