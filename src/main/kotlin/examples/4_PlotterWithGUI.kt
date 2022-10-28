package examples

import MemePlotter
import library.loadFeatures
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.TextParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import kotlin.math.log
import kotlin.math.max

fun main() = application {
    configure {
        width = 1280
        height = 1080
    }
    program {
        val tp = MemePlotter(12.0, 10.0)
        tp.setupMouseEvents(mouse)
        val logits = loadFeatures("datasets/attributes/mood-logits.csv")

        val gui = GUI()
        gui.compartmentsCollapsedByDefault = false

        val settings = object {
            @TextParameter("Prompt 1", order = 0)
            var prompt1 = "sexual"

            @TextParameter("Prompt 2", order = 1)
            var prompt2 = "dark"

        }.addTo(gui, "Settings")

        var maxLogitValue = 0.0
        logits.flatMap { it.value }.forEach { maxLogitValue = max(maxLogitValue, it) }

        extend(gui)
        extend {

            val logit1 = logits[settings.prompt1] ?: List(tp.memes.size) { 0.0 }
            val logit2 = logits[settings.prompt2] ?: List(tp.memes.size) { 0.0 }

            val logitsToPoints = logit1.zip(logit2).mapIndexed { index, it ->
                //Polar(360.0 * index / maxLogitValue, it.second + it.first).cartesian
                Vector2(it.first, it.second)
            }
            var bounds = logitsToPoints.bounds
            if (bounds.width == 0.0) {
                bounds = bounds.copy(bounds.corner.copy(x = -1.0), width = 2.0)
            }
            if (bounds.height == 0.0) {
                bounds = bounds.copy(bounds.corner.copy(y = -1.0), height = 2.0)
            }
            val points = logitsToPoints.map { it.map(bounds, drawer.bounds) }

            tp.positions = points
            tp.draw(drawer)
        }
    }
}