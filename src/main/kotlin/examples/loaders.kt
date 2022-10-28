import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import java.io.File

fun loadPositions(file: String): List<Vector2> {
    return csvReader().readAllWithHeader(File(file)).map {
        Vector2(it["0"]!!.toDouble(), it["1"]!!.toDouble())
    }
}

fun loadPositions3D(file: String): List<Vector3> {
    return csvReader().readAllWithHeader(File(file)).map {
        Vector3(it["x"]!!.toDouble(), it["y"]!!.toDouble(), it["z"]!!.toDouble())
    }
}


fun blendPositions(positions1: List<Vector2>, positions2: List<Vector2>, blendAmount: Double): List<Vector2> {

   return (positions1 zip positions2).map { it.first.mix(it.second, blendAmount) }

}