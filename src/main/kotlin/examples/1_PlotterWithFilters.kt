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

        val sizes = tp.memes.map { if (it.nsfw) 1.0 else 0.5 }
        val colors = tp.memes.map {
            when (it.year) {
                in 1990..2000 -> ColorRGBa.ORANGE_RED
                in 2001..2006 -> ColorRGBa.CORNFLOWER_BLUE
                in 2007..2012 -> ColorRGBa.LIGHT_SEA_GREEN
                in 2013..2016 -> ColorRGBa.PURPLE
                else -> ColorRGBa.PINK
            }.opacify(0.8)
        }

        tp.positionsFile = "datasets/positions/mood-tsne.csv"
        tp.sizes = sizes
        tp.colors = colors

        extend {
            tp.draw(drawer)
        }
    }
}
