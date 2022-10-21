package examples

import library.loadLogits
import org.openrndr.application
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {
    program {
        val logits = loadLogits("datasets/mood-logits-all.csv")

        val bla = logits["masculine"]!!.zip(logits["fuzzy"]!!).map {
            Vector2(it.first, it.second)
        }
        val bounds = bla.bounds
        val points = bla.map { it.map(bounds, drawer.bounds) }
        extend {

            drawer.circles(points, 5.0)

        }
    }
}