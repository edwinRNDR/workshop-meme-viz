package examples.aggregation

import library.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.camera.Camera2D
import kotlin.math.sqrt

fun main() = application {
    configure {
        width = 1024
        height = 1024
    }

    program {
        val features = loadFeatures("datasets/attributes/prompt-logits.csv")
        val mappers = features.minMaxMappers()

        val memes = loadMemes("datasets/memes-all.json")
        //val memesGrouped = memes.groupBy { it.origin }.filterValues { it.size > 1 }
        val memesGrouped = memes.groupByTag().filterValues { it.size > 50 }

        val feature = "a photograph of an ugly person"

        val groupStatistics = memesGrouped.featureStatistics(features)
        val groupStatisticsList = groupStatistics.toList().sortedByDescending { it.second[feature]!!.average }

        // (aggregate1 -> { "feature1" : Statistics, "feature2" : Statistics, ... }
        // (aggregate2 -> { "feature1" : Statistics, "feature2" : Statistics, ... }
        // (aggregate3 -> { "feature1" : Statistics, "feature2" : Statistics, ... }
        // (aggregate4 -> { "feature1" : Statistics, "feature2" : Statistics, ... }
        // (aggregate5 -> { "feature1" : Statistics, "feature2" : Statistics, ... }

        val statisticsGrid = GridView(groupStatisticsList, 4, 256.0, 256.0) { (key, value), state ->
            val mapper = mappers[feature]!!
            val statistics = value[feature]!!

            drawer.text("${(key ?: "no name")} (${statistics.sampleCount})"  , 0.0, 0.0)
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
            drawer.text("${(mapper(statistics.average)*100.0).format(2)}", 128.0 + radiusAverage + 10, 128.0)
        }

        extend(Camera2D())
        extend {
            statisticsGrid.draw(drawer)
        }
    }
}