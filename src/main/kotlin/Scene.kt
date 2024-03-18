package examples

import glm_.vec2.Vec2
import imgui.ImGui

object Scene {
    fun draw() {


        ImGui.backgroundDrawList.addRectFilled(Vec2(20, 30), Vec2(100, 200), ImGui.getColorU32(40, 150, 70, 255))

        if (UI.showMouse)
            ImGui.backgroundDrawList.addRectFilled(
                Vec2(
                    MouseSettings.topLeftX,
                    MouseSettings.topLeftY,
                ), Vec2(
                    MouseSettings.topLeftX + MouseSettings.width,
                    MouseSettings.topLeftY + MouseSettings.height
                ), ImGui.getColorU32(50, 100, 90, 255)
            )

    }
}