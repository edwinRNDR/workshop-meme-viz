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
        val camera = Camera2D()
        extend(camera)
        val tp = MemePlotter(12.0, 15.0)

        val sizes = tp.memes.map { if (it.nsfw) 1.0 else 0.5 }
        val colors = tp.memes.map {
            when (it.year) {
                in 1990..2000 -> ColorRGBa.ORANGE_RED
                in 2001..2006 -> ColorRGBa.CORNFLOWER_BLUE
                in 2007..2012 -> ColorRGBa.LIGHT_SEA_GREEN
                in 2013..2016 -> ColorRGBa.PURPLE
                else -> ColorRGBa.PINK
            }.opacify(0.5)
        }

        tp.positionsFile = "datasets/mood-tsne.csv"
        tp.sizes = sizes
        tp.colors = colors


        var activeMemes = listOf<Meme>()
        tp.plotterChange.listen { newMemes ->
            activeMemes = newMemes
        }

        extend {
            tp.draw(drawer, mouse.position)

            drawer.defaults()
            drawer.writer {
                var y = 0.0
                box = Rectangle(0.0, 0.0, 400.0, 400.0)
                newLine()
                activeMemes.forEach {
                    it.toList().forEach {
                        newLine()
                        text(it)
                    }
                }
            }

            drawer.stroke = ColorRGBa.YELLOW
            drawer.fill = null
            drawer.circle(mouse.position, tp.queryRadius * (camera.view.trace - 1.0) / 3.0)
        }
    }
}

/*


        val logits = loadLogits("datasets/mood-logits-all.csv")

//        val bla = logits["sexual"]!!.zip(logits["bad"]!!).map {
//            Vector2(it.first, it.second)
//        }

val bla = tp.memes.map { (it.year?:2000).toDouble().coerceAtLeast(2000.0) }.zip(logits["natural"]!!).map {
    Vector2(it.first, -it.second)
}

val bounds = bla.bounds
val points = bla.map { it.map(bounds, drawer.bounds) }*/
