package examples.multiview

import MemePlotter
import library.Meme
import library.ViewBox
import loadPositions
import org.openrndr.application
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.loadImage
import org.openrndr.extra.imageFit.FitMethod
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import org.openrndr.writer
import java.io.File

fun main() {
    application {

        configure {
            width = 1024
            height = 1024
        }
        program {
            var activeMemes = listOf<Meme>()
            var activeMemeImages = listOf<ColorBuffer>()

            val leftBox = ViewBox(this, Vector2(0.0, 0.0), width / 3, height) {
                for ((index, meme) in activeMemes.withIndex()) {
                    drawer.imageFit(activeMemeImages[index], Rectangle(0.0, 0.0, width/3.0, 200.0), fitMethod = FitMethod.Contain )
                    drawer.translate(0.0, 220.0)
                    writer {
                        box = Rectangle(0.0, 0.0, width / 3.0, 40.0)
                        text(meme.name)
                    }
                    drawer.translate(0.0, 60.0)
                }
            }

            val positions = loadPositions("datasets/positions/prompt-tsne.csv").let {
                it.map(it.bounds, Rectangle(0.0, 0.0, width * (2.0 / 3.0), height.toDouble()))
            }

            val plotter = MemePlotter(queryRadius = 10.0, positions = positions)

            val rightBox = ViewBox(this, Vector2(width / 3.0, 0.0), (2 * width) / 3, height) {
                plotter.draw(drawer)
            }
            plotter.setupMouseEvents(rightBox.mouse)
            plotter.plotterChange.listen { it ->
                activeMemes = it

                activeMemeImages.forEach { image ->
                    image.destroy()
                }
                activeMemeImages = it.map {
                    loadImage(File("scraped-memes/${it.id}/meme-image.jpg"))
                }
            }

            extend {
                leftBox.draw()
                rightBox.draw()
            }
        }
    }
}