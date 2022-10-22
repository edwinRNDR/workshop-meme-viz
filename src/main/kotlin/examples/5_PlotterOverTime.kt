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

        val prompt = logits["natural"]?: List(tp.memes.size) { 0.0 }
        val positionsOverTime = tp.memes.map { (it.year?:2000).toDouble().coerceAtLeast(2000.0) }.zip(prompt).map {
            Vector2(it.first, -it.second)
        }
        val bounds = positionsOverTime.bounds
        val points = positionsOverTime.map { it.map(bounds, drawer.bounds) }

        tp.positions = points

        extend {
            tp.draw(drawer)
        }
    }
}