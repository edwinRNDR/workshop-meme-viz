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
        val memesGrouped = memes.groupBy { it.origin }.filterValues { it.size > 1 }

        val groupStatistics = memesGrouped.mapValues {
            val memeIndices = it.value.map { v -> v.index }
            val featuresGrouped = memeIndices.map { index -> features.row(index) }.transpose()
            featuresGrouped.statistics()
        }.toList()

        var feature = "a photograph of a young man"

        val statisticsGrid = GridView(groupStatistics, 4, 256.0, 256.0) { (key, value), state ->
            drawer.text(key ?: "no name", 0.0, 0.0)
            drawer.fill = ColorRGBa.WHITE
            val p95 = value[feature]!!.p95
            val radiusP95 = sqrt(mappers[feature]!!(p95)) * 100.0
            drawer.circle(128.0, 128.0,  radiusP95)

            val p5 = value[feature]!!.p5
            val radiusP5 = sqrt( mappers[feature]!!(p5)) * 100.0
            drawer.circle(128.0, 128.0,  radiusP5)
        }

        extend(Camera2D())
        extend {
            statisticsGrid.draw(drawer)
        }
    }
}