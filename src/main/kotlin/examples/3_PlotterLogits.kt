package examples

import MemePlotter
import library.loadLogits
import org.openrndr.application
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

        val logits = loadLogits("datasets/mood-logits-all.csv")

        val logitsToPoints = logits["sexual"]!!.zip(logits["bad"]!!).map {
            Vector2(it.first, it.second)
        }
        val bounds = logitsToPoints.bounds
        val points = logitsToPoints.map { it.map(bounds, drawer.bounds) }

        tp.positions = points

        extend {
            tp.draw(drawer)
        }
    }
}