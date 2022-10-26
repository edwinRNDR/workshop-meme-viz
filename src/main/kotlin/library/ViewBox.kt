package library


import org.openrndr.Program
import org.openrndr.color.ColorRGBa.Companion.TRANSPARENT
import org.openrndr.draw.*
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2

class ViewBox(
    val program: Program,
    val position: Vector2,
    val width: Int,
    val height: Int,
    val drawFunction: () -> Unit
) {
    var view = Matrix44.IDENTITY

    val target = renderTarget(width, height, contentScale = RenderTarget.active.contentScale) {
        colorBuffer()
        depthBuffer()
    }

    fun draw() {
        val drawer = program.drawer
        drawer.isolatedWithTarget(target) {
            drawer.clear(TRANSPARENT)
            drawFunction()
        }
        drawer.isolated {
            drawer.defaults()
            drawer.image(target.colorBuffer(0), position)
        }
    }
}