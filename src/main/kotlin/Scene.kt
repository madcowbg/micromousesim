package examples

import org.lwjgl.opengl.GL11

object Scene {
    fun draw() {


        GL11.glColor3f(0f, 0f, 0f)
        GL11.glRecti(Settings.topLeftX, Settings.topLeftY, Settings.bottomRightX, Settings.bottomRightY)
        
    }
}