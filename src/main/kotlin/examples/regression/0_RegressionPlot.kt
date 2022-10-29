package examples.regression

import MemePlotter
import library.loadFeatures
import library.loadMemes
import org.openrndr.application
import org.openrndr.draw.isolated
import org.openrndr.draw.loadFont
import org.openrndr.extra.camera.Camera2D
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map

fun main() {
    application {

        configure {
            width = 1024
            height = 1024
        }

        program {

            val features = loadFeatures("datasets/regressions/regression.csv")
            val memes = loadMemes("datasets/memes-all.json")
            val positions = MutableList(memes.size) { Vector2.ZERO}

            val predictedYears = features["predicted year"]!!
            val counts = mutableMapOf<Int, Int>()

            for (i in 0 until predictedYears.size) {
                val year = predictedYears[i].toInt()
                val c = counts.getOrPut(year) { 0 }
                counts[year] = c + 1
                positions[i] = Vector2((year-1990.0) * 32.0, c * -32.0 + height-100.0)
            }

            val tp = MemePlotter(12.0, 15.0, positions)


            extend(Camera2D())
            extend {

                tp.draw(drawer)
                for (i in 1990 until 2030) {

                    drawer.isolated {

                        drawer.translate((i-1990.0) * 32.0, height- 80.0)
                        drawer.rotate(90.0)
                        drawer.text(i.toString())

                    }
                }

            }
        }
    }
}