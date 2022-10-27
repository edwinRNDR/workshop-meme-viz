package library


import org.openrndr.MouseEvent
import org.openrndr.Program
import org.openrndr.color.ColorRGBa.Companion.TRANSPARENT
import org.openrndr.draw.*
import org.openrndr.events.Event
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.buildTransform
import org.openrndr.shape.Rectangle

class ViewBox(
    val program: Program,
    val position: Vector2,
    val width: Int,
    val height: Int,
    val drawFunction: () -> Unit
) {
    var view = Matrix44.IDENTITY
    val clientArea = Rectangle(position, width.toDouble(), height.toDouble())

    val mouseDragged = Event<MouseEvent>()
    val mouseButtonDown = Event<MouseEvent>()
    val mouseScrolled = Event<MouseEvent>()

    init {
        program.mouse.dragged.listen {
            if (it.position in clientArea) {
                mouseDragged.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.scrolled.listen {
            if (it.position in clientArea) {
                mouseScrolled.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.buttonDown.listen {
            if (it.position in clientArea) {
                mouseButtonDown.trigger(it.copy(position = it.position - position))
            }
        }

        mouseDragged.listen {
            view = buildTransform { translate(it.dragDisplacement) } * view
        }
        mouseScrolled.listen {
            val scaleFactor = 1.0 - it.rotation.y * 0.03
            view = buildTransform {
                translate(it.position)
                scale(scaleFactor)
                translate(-it.position)
            } * view
        }

    }

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