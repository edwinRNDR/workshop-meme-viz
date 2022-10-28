package examples.multiview

import MemePlotter3D
import library.ViewBox3D
import loadPositions3D
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {
        val positions = loadPositions3D("datasets/positions/prompt-umap-spherical.csv").map { it * 30.0 }
        val scatterPlot = MemePlotter3D(5.0, 15.0, positions)
        val viewBox = ViewBox3D(this, Vector2(100.0, 100.0), width - 200, height - 200) {
            drawer.clear(ColorRGBa.PINK)
            scatterPlot.draw(drawer)
        }
        extend {
            viewBox.draw()
        }
    }
}
