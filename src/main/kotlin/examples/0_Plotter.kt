package examples

import MemePlotter
import org.openrndr.application

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        val tp = MemePlotter(12.0)

        val sizes = tp.memes.map { if (it.nsfw) 1.0 else 0.0 }
        tp.positionsFile = "datasets/bert-tsne.csv"
        //tp.sizes = sizes

        extend {
            tp.draw(drawer, mouse.position)
        }
    }
}
