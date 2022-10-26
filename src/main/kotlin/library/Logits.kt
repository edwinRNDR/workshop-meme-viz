package library

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

fun loadFeatures(filename: String) : Map<String, List<Double>> {
    val result = mutableMapOf<String, MutableList<Double>>()
    csvReader().open(filename) {
        this.readAllWithHeaderAsSequence().forEach {
            it.forEach {  e ->
                val list = result.getOrPut(e.key) { mutableListOf() }
                list.add(e.value.toDouble())
            }
        }
    }
    return result
}