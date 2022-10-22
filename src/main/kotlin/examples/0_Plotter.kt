package examples

import MemePlotter
import library.Meme
import library.loadLogits
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.writer
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.presets.*
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        val tp = MemePlotter(12.0, 15.0)

        tp.positionsFile = "datasets/bert-umap.csv"

        extend {

            tp.draw(drawer)

        }
    }
}
