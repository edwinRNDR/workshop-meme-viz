package examples

import MemePlotter
import blendPositions
import library.Meme
import loadPositions
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.writer
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.presets.*
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import kotlin.math.sin

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        val tp = MemePlotter(10.0)

        val positions1 = loadPositions("datasets/positions/prompt-grid.csv")
        val positions2 = loadPositions("datasets/positions/prompt-tsne.csv")

        extend {
            val t = mouse.position.x / width
            val positions = blendPositions(positions1, positions2, t)
            tp.positions = positions.map(positions.bounds, drawer.bounds)
            tp.draw(drawer)
        }
    }
}
