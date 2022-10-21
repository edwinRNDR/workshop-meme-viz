package examples

import MemePlotter
import library.loadLogits
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
        extend(Camera2D())
        val tp = MemePlotter(12.0)

        val sizes = tp.memes.map { if (it.nsfw) 1.0 else 0.0 }


        val logits = loadLogits("datasets/mood-logits-all.csv")


//        val bla = logits["sexual"]!!.zip(logits["bad"]!!).map {
//            Vector2(it.first, it.second)
//        }

        val bla = tp.memes.map { (it.year?:2000).toDouble().coerceAtLeast(2000.0) }.zip(logits["natural"]!!).map {
            Vector2(it.first, -it.second)
        }

        val bounds = bla.bounds
        val points = bla.map { it.map(bounds, drawer.bounds) }

        tp.positions = points
        //tp.positionsFile = "datasets/bert-tsne.csv"
        //tp.sizes = sizes

        extend {
            tp.draw(drawer, mouse.position)
        }
    }
}
