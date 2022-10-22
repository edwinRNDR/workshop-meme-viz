package examples

import MemePlotter
import library.Meme
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.writer
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.presets.*
import org.openrndr.shape.Rectangle

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        val tp = MemePlotter(12.0, 15.0)

        tp.positionsFile = "datasets/mood-tsne.csv"

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
