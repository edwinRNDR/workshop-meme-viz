package library


import org.openrndr.MouseButton
import org.openrndr.MouseEvent
import org.openrndr.MouseEvents
import org.openrndr.Program
import org.openrndr.color.ColorRGBa.Companion.TRANSPARENT
import org.openrndr.draw.*
import org.openrndr.events.Event
import org.openrndr.extra.camera.OrbitalCamera
import org.openrndr.extra.camera.OrbitalControls
import org.openrndr.extra.camera.ProjectionType
import org.openrndr.extra.camera.isolated
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.asRadians
import org.openrndr.math.transforms.buildTransform
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.tan

class ViewBox3D(
    val program: Program,
    val position: Vector2,
    val width: Int,
    val height: Int,
    val drawFunction: () -> Unit
) {
    var view = Matrix44.IDENTITY
    val clientArea = Rectangle(position, width.toDouble(), height.toDouble())

    val orbitalCamera = OrbitalCamera(eye = Vector3.UNIT_Z * 10.0)

    enum class STATE {
        NONE,
        ROTATE,
        PAN,
    }

    private var state = STATE.NONE

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

    var lastMousePosition = Vector2.ZERO
    var dragStart = Vector2.INFINITY

    init {
        program.mouse.moved.listen {
            if (it.position in clientArea) {
                val event = it
                if (state != STATE.NONE) {
                    val delta = lastMousePosition - (event.position - position)
                    lastMousePosition = event.position - position

                    if (state == STATE.PAN) {

                        val offset = Vector3.fromSpherical(orbitalCamera.spherical) - orbitalCamera.lookAt

                        // half of the fov is center to top of screen
                        val targetDistance = offset.length * tan(orbitalCamera.fov.asRadians / 2)
                        val panX = (2 * delta.x * targetDistance / width)
                        val panY = (2 * delta.y * targetDistance / height)

                        orbitalCamera.pan(panX, -panY, 0.0)

                    } else {
                        val rotX = 360.0 * delta.x / width
                        val rotY = 360.0 * delta.y / height
                        orbitalCamera.rotate(rotX, rotY)
                    }
                }

                mouse.moved.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.dragged.listen {
            if (dragStart in clientArea) {
                view = buildTransform { translate(it.dragDisplacement) } * view
                mouse.dragged.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.scrolled.listen {
            val event = it
            if (!it.propagationCancelled && it.position in clientArea) {
                if (true) {

                    if (orbitalCamera.projectionType == ProjectionType.PERSPECTIVE) {
                        if (abs(event.rotation.x) <= 0.1) {
                            when {
                                event.rotation.y > 0 -> orbitalCamera.dollyIn()
                                event.rotation.y < 0 -> orbitalCamera.dollyOut()
                            }
                        }
                    } else {
                        if (abs(event.rotation.x) <= 0.1) {
                            when {
                                event.rotation.y > 0 -> orbitalCamera.scale(1.0)
                                event.rotation.y < 0 -> orbitalCamera.scale(-1.0)
                            }
                        }
                    }
                }

                val scaleFactor = 1.0 - it.rotation.y * 0.03
                view = buildTransform {
                    translate(it.position - position)
                    scale(scaleFactor)
                    translate(-(it.position - position))
                } * view
                mouse.scrolled.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.buttonDown.listen {
            dragStart = it.position
            if (it.position in clientArea) {
                val previousState = state

                when (it.button) {
                    MouseButton.LEFT -> {
                        state = STATE.ROTATE
                    }
                    MouseButton.RIGHT -> {
                        state = STATE.PAN
                    }
                    MouseButton.CENTER -> {
                    }
                    MouseButton.NONE -> {
                    }
                }

                if (previousState == STATE.NONE) {
                    lastMousePosition = it.position - position
                }

                mouse.buttonDown.trigger(it.copy(position = it.position - position))
            }
        }

        program.mouse.buttonUp.listen {
            state = STATE.NONE
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
        orbitalCamera.update(1.0/60.0)
        drawer.isolatedWithTarget(target) {
            drawer.defaults()
            drawer.perspective(orbitalCamera.fov, width*1.0/height, 0.1, 1000.0)
            drawer.view = orbitalCamera.viewMatrix()
            drawer.depthTestPass = DepthTestPass.LESS_OR_EQUAL
            drawer.depthWrite = true
                drawer.clear(TRANSPARENT)
                drawFunction()
        }
        drawer.isolated {
            drawer.defaults()
            drawer.image(target.colorBuffer(0), position)
        }
    }
}