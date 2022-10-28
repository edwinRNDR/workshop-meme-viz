package examples.multiview

import MemePlotter
import library.ViewBox
import loadPositions
import org.openrndr.application
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() {
    application {
        configure {
            width = 1280
            height = 720
        }
        program {

            var positions = loadPositions("datasets/positions/bert-tsne.csv")
            positions = positions.map(positions.bounds, drawer.bounds)

            val tp = MemePlotter(12.0, 15.0, positions)


            val leftBox =  ViewBox(this, Vector2(width/2.0, 0.0), width/2, height) {
                drawer.rectangle(100.0, 100.0, 100.0, 100.0)
            }


            val rightBox =  ViewBox(this, Vector2(0.0, 0.0), width/2, height) {
                tp.draw(drawer)
            }
            tp.setupMouseEvents(rightBox.mouse)

            extend {
                leftBox.draw()
                rightBox.draw()
            }


        }
    }
}