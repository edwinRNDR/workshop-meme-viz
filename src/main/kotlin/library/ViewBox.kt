package library


import org.openrndr.MouseEvent
import org.openrndr.MouseEvents
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
    var enableHorizontalPan: Boolean = true,
    var enableVerticalPan: Boolean = true,
    var enableZoom: Boolean = true,
    val drawFunction: () -> Unit
) {
    var view = Matrix44.IDENTITY
    val clientArea = Rectangle(position, width.toDouble(), height.toDouble())

    class Mouse : MouseEvents {
        override val buttonDown = Event<MouseEvent>()
        override val buttonUp = Event<MouseEvent>()
        override val dragged = Event<MouseEvent>()
        override val entered = Event<MouseEvent>()
        override val exited = Event<MouseEvent>()
        override val moved = Event<MouseEvent>()
        override val scrolled = Event<MouseEvent>()
    }

    val mouse = Mouse()

    var dragStart = Vector2.INFINITY

    init {
        program.mouse.moved.listen {
            if (it.position in clientArea) {
                mouse.moved.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.dragged.listen {
            if (dragStart in clientArea) {
                if (enableHorizontalPan && enableVerticalPan) {
                    view = buildTransform { translate(it.dragDisplacement) } * view
                } else if (enableHorizontalPan) {
                    view = buildTransform { translate(it.dragDisplacement.copy(y = 0.0)) } * view
                } else if (enableVerticalPan) {
                    view = buildTransform { translate(it.dragDisplacement.copy(x = 0.0)) } * view
                }
                mouse.dragged.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.scrolled.listen {
            if (it.position in clientArea) {
                if (enableZoom) {
                    val scaleFactor = 1.0 - it.rotation.y * 0.03
                    view = buildTransform {
                        translate(it.position - position)
                        scale(scaleFactor)
                        translate(-(it.position - position))
                    } * view
                }
                mouse.scrolled.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.buttonDown.listen {
            dragStart = it.position
            if (it.position in clientArea) {
                mouse.buttonDown.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.buttonUp.listen {
            dragStart = it.position
            if (it.position in clientArea) {
                mouse.buttonUp.trigger(it.copy(position = it.position - position))
            }
        }
    }

    val target = renderTarget(width, height, contentScale = RenderTarget.active.contentScale) {
        colorBuffer()
        depthBuffer()
    }

    fun draw() {
        val drawer = program.drawer
        drawer.isolatedWithTarget(target) {
            drawer.defaults()
            drawer.ortho(target)
            drawer.clear(TRANSPARENT)
            drawer.view = this@ViewBox.view
            drawFunction()
        }
        drawer.isolated {
            drawer.defaults()
            drawer.image(target.colorBuffer(0), position)
        }
    }
}