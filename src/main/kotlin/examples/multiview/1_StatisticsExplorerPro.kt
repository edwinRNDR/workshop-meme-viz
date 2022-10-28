package examples.multiview

import MemePlotter
import library.*
import loadPositions
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.camera.Camera2D
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import kotlin.math.sqrt

fun main() = application {
    configure {
        width = 1400
        height = 1024
    }

    program {
        val features = loadFeatures("datasets/attributes/prompt-logits.csv")
        val featureMappers = features.minMaxMappers()

        val memes = loadMemes("datasets/memes-all.json")
        val memesGrouped = memes.groupBy { it.origin }.filterValues { it.size > 1 }

        val groupStatistics = memesGrouped.mapValues {
            val memeIndices = it.value.map { v -> v.index }
            val featuresGrouped = memeIndices.map { index -> features.row(index) }.transpose()
            featuresGrouped.statistics()
        }.toList()

        val featureNames = features.keys.toList()

        val featuresGrid = GridView(featureNames, 1, 256.0, 64.0) { feature, state ->
            if (state.active) {
                drawer.fill = ColorRGBa.PINK
                drawer.rectangle(0.0, 0.0, 256.0, 64.0)
            }
            if (state.hover) {
                drawer.fill = ColorRGBa.WHITE.opacify(0.5)
                drawer.rectangle(0.0, 0.0, 256.0, 64.0)
            }
            drawer.fill = ColorRGBa.WHITE
            drawer.text(feature, 20.0, 20.0)
        }

        var activeFeature = featureNames[0]

        val statisticsGrid = GridView(groupStatistics, 4, 256.0, 256.0) { (key, value), state ->
            if (state.active) {
                drawer.fill = ColorRGBa.PINK
                drawer.rectangle(0.0, 0.0, 256.0, 256.0)
            }
            if (state.hover) {
                drawer.fill = ColorRGBa.WHITE.opacify(0.5)
                drawer.rectangle(0.0, 0.0, 256.0, 256.0)
            }

            drawer.fill = ColorRGBa.WHITE
            drawer.text( " ${key ?: "no name"} (${value[activeFeature]!!.sampleCount}" , 0.0, 20.0)

            val p95 = value[activeFeature]!!.p95
            val radiusP95 = sqrt(featureMappers[activeFeature]!!(p95)) * 100.0
            drawer.circle(128.0, 128.0, radiusP95)

            val average = value[activeFeature]!!.average
            val radiusAverage = sqrt(featureMappers[activeFeature]!!(average)) * 100.0
            drawer.circle(128.0, 128.0, radiusAverage)

            val p5 = value[activeFeature]!!.p5
            val radiusP5 = sqrt(featureMappers[activeFeature]!!(p5)) * 100.0
            drawer.circle(128.0, 128.0, radiusP5)
        }

        var positions = loadPositions("datasets/positions/prompt-tsne.csv")
        positions = positions.map(positions.bounds, drawer.bounds)

        val scatterPlot = MemePlotter(12.0, 15.0, positions)

        featuresGrid.activeItemChanged.listen {
            activeFeature = it
            statisticsGrid.activeItemChanged.trigger(statisticsGrid.activeItem!!)
        }

        statisticsGrid.activeItemChanged.listen { item ->
            val mapper = featureMappers[activeFeature]!!
            val sizes = List(memes.size) { index -> mapper(features[activeFeature]!![index]) * 0.9 + 0.1 }
            val colors =  memes.mapIndexed { index, it ->
                if (it.origin == item.first) {
                    val value = mapper(features[activeFeature]!![index])
                    ColorRGBa.BLUE.mix(ColorRGBa.RED, value)
                } else {
                    ColorRGBa.WHITE.shade(0.2).opacify(0.1)
                }
            }
            scatterPlot.sizes = sizes
            scatterPlot.colors = colors
        }

        val leftBox = ViewBox(this, Vector2(0.0, 0.0), 256, height, enableZoom = false, enableHorizontalPan = false) {
            featuresGrid.draw(drawer)
        }

        val midBox = ViewBox(this, Vector2(256.0, 0.0), (width-256)/2, height) {
            statisticsGrid.draw(drawer)
        }

        val rightBox = ViewBox(this, Vector2(256.0 + (width-256)/2, 0.0), (width-256)/2, height) {
            scatterPlot.draw(drawer)
        }

        featuresGrid.setupMouseEvents(leftBox.mouse)
        statisticsGrid.setupMouseEvents(midBox.mouse)

        extend(Camera2D())
        extend {
            leftBox.draw()
            midBox.draw()
            rightBox.draw()
        }
    }
}