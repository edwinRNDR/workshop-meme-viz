package examples

import MemePlotter
import library.Meme
import loadPositions
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.writer
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.presets.*
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        var positions = loadPositions("datasets/positions/mood-tsne.csv")
        positions = positions.map(positions.bounds, drawer.bounds)

        val tp = MemePlotter(12.0, 15.0, positions)


        var activeMemes = listOf<Meme>()
        tp.plotterChange.listen { newMemes ->
            activeMemes = newMemes
        }

        val camera = Camera2D()
        extend(camera)
        extend {
            tp.draw(drawer, mouse.position)

            drawer.defaults()
            drawer.writer {
                box = Rectangle(0.0, 0.0, 400.0, 400.0)
                newLine()
                activeMemes.forEach {
                    it.toList().forEach { text ->
                        newLine()
                        text(text)
                    }
                }
            }

            drawer.stroke = ColorRGBa.YELLOW
            drawer.fill = null
            drawer.circle(mouse.position, tp.queryRadius * (camera.view.trace - 1.0) / 3.0)
        }
    }
}
