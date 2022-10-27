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
        val tp = MemePlotter(12.0)

        val positions1 = loadPositions("datasets/positions/bert-tsne.csv")
        val positions2 = loadPositions("datasets/positions/mood-tsne.csv")

        extend {

            val t = sin(seconds * 0.3) * 0.5 + 0.5
            val positions = blendPositions(positions1, positions2, t)
            tp.positions = positions.map(positions.bounds, drawer.bounds)
            tp.draw(drawer)

        }
    }
}
