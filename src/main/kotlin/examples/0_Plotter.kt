package examples

import MemePlotter
import loadPositions
import org.openrndr.application
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        var positions = loadPositions("datasets/positions/bert-tsne.csv")
        positions = positions.map(positions.bounds, drawer.bounds)

        val tp = MemePlotter(12.0, 15.0, positions)


        extend {

            tp.draw(drawer)

        }
    }
}
