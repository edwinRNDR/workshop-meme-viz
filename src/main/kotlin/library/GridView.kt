package library

import org.openrndr.MouseEvents
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle

class CellState(val hover: Boolean, val active: Boolean)

class GridView<T>(
    var items: List<T>,
    var columns: Int = 3,
    var cellWidth: Double = 100.0,
    var cellHeight: Double = 100.0,
    val draw: (item: T, state: CellState) -> Unit
) {
    val activeItemChanged = Event<T>()

    var activeItem: T? = items.firstOrNull()
        set(value) {
            if (value != field) {
                field = value
                if (value != null) {
                    activeItemChanged.trigger(value)
                }
            }
        }


    var mousePosition = Vector2.INFINITY

    var lastView = Matrix44.IDENTITY
    fun setupMouseEvents(mouse: MouseEvents) {
        mouse.moved.listen {
            mousePosition = it.position
        }

        mouse.buttonUp.listen {
            val cursorPosition = (lastView.inversed * it.position.xy01).div.xy
            for ((index, item) in items.withIndex()) {
                val i = index % columns
                val j = index / columns
                val x = i * cellWidth
                val y = j * cellHeight
                val cell = Rectangle(x, y, cellWidth, cellHeight)
                if (cursorPosition in cell) {
                    activeItem = item
                    break
                }
            }
        }
    }

    fun draw(drawer: Drawer) {
        lastView = drawer.view
        val cursorPosition = (drawer.view.inversed * mousePosition.xy01).div.xy
        for ((index, item) in items.withIndex()) {
            val i = index % columns
            val j = index / columns
            val x = i * cellWidth
            val y = j * cellHeight
            val cell = Rectangle(x, y, cellWidth, cellHeight)

            drawer.isolated {
                drawer.translate(x, y)
                draw(item, CellState(cursorPosition in cell, item === activeItem))
            }
        }
    }

}