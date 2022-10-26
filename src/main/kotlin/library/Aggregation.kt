package library

import org.openrndr.math.map
import kotlin.math.sqrt


data class Statistics(val average:Double, val variance:Double, val standardDeviation: Double, val p5:Double, val p95: Double)


fun Map<String, List<Double>>.minMaxMappers(): Map<String, (Double)->Double> {
    val min = this.percentile(0.0)
    val max = this.percentile(1.0)

    return this.mapValues {
        { x:Double ->
             x.map(min[it.key]!!, max[it.key]!!, 0.0, 1.0)
        }
    }
}


fun Map<String, List<Double>>.row(index: Int): Map<String, Double> {
    return this.mapValues { it.value[index] }
}

fun List<Map<String, Double>>.transpose(): Map<String, List<Double>> {
    val r = this.first().mapValues { (key, _) ->
        this.map { r -> r[key]!! }
    }
    return r
}

fun Map<String, List<Double>>.average(): Map<String, Double> {
    return this.mapValues {
        if (it.value.isNotEmpty()) {
            it.value.sum() / it.value.size
        } else {
            0.0
        }
    }
}

fun Map<String, List<Double>>.percentile(p:Double): Map<String, Double> {
    return this.mapValues {
        if (it.value.isNotEmpty()) {
            it.value.sorted()[(it.value.size * p).toInt().coerceIn(0, it.value.lastIndex)]
        } else {
            0.0
        }
    }
}

fun Map<String, List<Double>>.variance(): Map<String, Double> {
    val average = this.average()
    val r = this.mapValues { entry ->
        if (entry.value.size > 1) {
            entry.value.sumOf {
                val x = it - average[entry.key]!!
                x * x
            } / (entry.value.size - 1.0)

        } else {
            0.0
        }
    }
    return r
}


fun Map<String, List<Double>>.statistics(): Map<String, Statistics> {
    val averarage = this.average()
    val variance = this.variance()
    val p5 = this.percentile(0.05)
    val p95 = this.percentile(0.95)
    val standardDeviation = variance.mapValues { sqrt(it.value) }

    return this.mapValues { (key, value) ->

        Statistics(averarage[key]!!, variance[key]!!, standardDeviation[key]!!, p5[key]!!, p95[key]!!)

    }
}
