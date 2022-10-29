package examples.multiview

import library.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.camera.Camera2D
import org.openrndr.math.Vector2
import kotlin.math.sqrt

fun main() = application {

    configure {
        width = 1024
        height = 1024
    }

    program {
        val features = loadFeatures("datasets/attributes/prompt-logits.csv")
        val featureMappers = features.minMaxMappers()

        val memes = loadMemes("datasets/memes-all.json")
        val memesGrouped = memes.groupBy { it.origin }.filterValues { it.size > 1 }
        val groupStatistics = memesGrouped.featureStatistics(features).toList()


        val groupGrid = GridView(groupStatistics, 1, 256.0, 64.0) { (name, statistics), state ->
            if (state.active) {
                drawer.fill = ColorRGBa.PINK
                drawer.rectangle(0.0, 0.0, 256.0, 64.0)
            }
            if (state.hover) {
                drawer.fill = ColorRGBa.WHITE.opacify(0.5)
                drawer.rectangle(0.0, 0.0, 256.0, 64.0)
            }
            drawer.fill = ColorRGBa.WHITE
            drawer.text(name?:"no name", 20.0, 20.0)
        }

        var activeGroup = groupStatistics[0]
        val statisticsGrid = GridView(activeGroup.second.toList().sortedByDescending { it.second.average }, 4, 256.0, 256.0) { (feature, statistics), state ->

            val mapper = featureMappers[feature]!!

            drawer.text("${(feature)}", 0.0, 0.0)
            drawer.fill = ColorRGBa.WHITE

            val radiusP95 = sqrt((mapper(statistics.p95))) * 100.0
            drawer.circle(128.0, 128.0,  radiusP95)

            val radiusAverage = sqrt( mapper(statistics.average)) * 100.0
            drawer.strokeWeight = 3.0
            drawer.circle(128.0, 128.0,  radiusAverage)

            val radiusP5 = sqrt( mapper(statistics.p5)) * 100.0
            drawer.strokeWeight = 1.0
            drawer.circle(128.0, 128.0,  radiusP5)

            drawer.fill = ColorRGBa.RED
            drawer.text("${((statistics.average)*100.0).format(2)}", 128.0 + radiusAverage + 10, 128.0)
        }


        groupGrid.activeItemChanged.listen {
            activeGroup = it
            statisticsGrid.items = activeGroup.second.toList().sortedByDescending { it.second.average }
        }



        val rightBox = ViewBox(this, Vector2(256.0, 0.0), width - 256, height) {
            statisticsGrid.draw(drawer)
        }

        val leftBox = ViewBox(this, Vector2(0.0, 0.0), 256, height, enableZoom = false, enableHorizontalPan = false) {
            groupGrid.draw(drawer)
        }
        groupGrid.setupMouseEvents(leftBox.mouse)


        extend {
            rightBox.draw()
            leftBox.draw()
        }
    }
}