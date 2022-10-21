import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import library.loadMemes
import org.openrndr.draw.*
import org.openrndr.extra.kdtree.kdTree
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import java.io.File
import java.io.FileReader

class MemePlotter(imageScale: Double = 10.0) {
    val memeImage = loadImage("datasets/tiles-64.png")

    var positionsFile = "datasets/bert-umap.csv"
        set(value) {
            field = value
            positions = loadData(value)
            kdtree = positions.kdTree()
            pointIndices = positions.indices.map { Pair(positions[it], it) }.associate { it }
        }

    var positions = loadData(positionsFile)
        set(value) {
            field = value
            positionInstances.apply {
                put {
                    for (pos in value) {
                        write(pos)
                    }
                }
            }
        }
    var sizes = positions.map { 1.0 }
        set(value) {
            field = value
            sizeInstances.apply {
                println("${positions.size}, ${value.size}")
                put {
                    for (size in value) {
                        write(size.toFloat())
                    }
                }
            }
        }

    var kdtree = positions.kdTree()
    var pointIndices = positions.indices.map { Pair(positions[it], it) }.associate { it }
    var memes = loadMemes("datasets/memes-all.json")

    private fun loadData(file: String): List<Vector2> {
        val frame = Rectangle(0.0, 0.0,1080.0, 1080.0)
        val pos = csvReader().readAll(File(file)).map {
            Vector2(it[1].toDouble(), it[2].toDouble())
        }
        return pos.map(pos.bounds, frame)
    }

    val quad = vertexBuffer(vertexFormat {
        position(3)
        textureCoordinate(2)
    }, 4).also {
        it.put {
            write(Vector3(-1.0, -1.0, 0.0))
            write(Vector2(0.0, 0.0))
            write(Vector3(1.0, -1.0, 0.0))
            write(Vector2(1.0, 0.0))
            write(Vector3(-1.0, 1.0, 0.0))
            write(Vector2(0.0, 1.0))
            write(Vector3(1.0, 1.0, 0.0))
            write(Vector2(1.0, 1.0))

        }
    }

    val positionInstances = vertexBuffer(vertexFormat {
        attribute("offset", VertexElementType.VECTOR2_FLOAT32)
    }, positions.size).also {
        it.put {
            for (pos in positions) {
                write(pos)
            }
        }
    }

    val sizeInstances = vertexBuffer(vertexFormat {
        attribute("size", VertexElementType.FLOAT32)
    }, positions.size).also {
        it.put {
            for (j in positions.indices) {
                write(1.0f)
            }
        }
    }

    val style = shadeStyle {
        vertexTransform = """
            x_position.xy *= p_scale * i_size;
            x_position.xy += i_offset.xy;
        """.trimIndent()

        fragmentTransform = """
            float itemsPerRow = textureSize(p_texture,0).x / p_tileSize;
            float tx = mod((c_instance * 1.0) / itemsPerRow, 1.0) + va_texCoord0.x/itemsPerRow; 
            float ty = floor(c_instance / itemsPerRow)/itemsPerRow + va_texCoord0.y / itemsPerRow;
            ty = 1.0 - ty;
            x_fill = texture(p_texture, vec2(tx, ty));
        """.trimIndent()

        parameter("texture", memeImage)
        parameter("tileSize", 64.0)
        parameter("scale", imageScale.coerceAtLeast(1.0))
    }

    var currentIndex = 0

    fun draw(drawer: Drawer, mousePos: Vector2) {
        drawer.isolated {

            val closest = kdtree.findNearest(mousePos)
            closest?.let {
                val indexOfClosest = pointIndices[closest]
                if(sizes[indexOfClosest!!] != 0.0) {
                    currentIndex = indexOfClosest
                }
            }

            drawer.writer {
                box = Rectangle(0.0, 0.0, 400.0, 400.0)
                newLine()
                memes[currentIndex].toList().forEach {
                    newLine()
                    text(it)
                }
            }
            style.parameter("currentIndex", currentIndex)

            drawer.shadeStyle = style
            drawer.vertexBufferInstances(
                listOf(quad),
                listOf(positionInstances, sizeInstances),
                DrawPrimitive.TRIANGLE_STRIP,
                positionInstances.vertexCount
            )
        }
    }
}