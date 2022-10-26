package examples

import MemePlotter
import library.loadFeatures
import org.openrndr.application
import org.openrndr.extra.camera.Camera2D
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {

        val tp = MemePlotter(12.0, 15.0)

        val logits = loadFeatures("datasets/attributes/prompt-logits.csv")
        println(logits.keys)

        val logitsToPoints = logits["a photograph of a man"]!!.zip(logits["a photograph of a group of people"]!!).map {
            Vector2(it.first, it.second)
        }
        val bounds = logitsToPoints.bounds
        val points = logitsToPoints.map { it.map(bounds, drawer.bounds) }

        tp.positions = points

        extend(Camera2D())
        extend {
            tp.draw(drawer)
        }
    }
}