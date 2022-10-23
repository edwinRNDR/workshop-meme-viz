import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import library.Meme
import library.loadMemes
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.events.Event
import org.openrndr.extra.kdtree.kdTree
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.Vector4
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import java.io.File
import java.io.FileReader

class MemePlotter(imageScale: Double = 10.0, var queryRadius: Double = 10.0) {
    val memeImage = loadImage("datasets/tiles-64.png")

    var positionsFile = "datasets/positions/bert-tsne.csv"
        set(value) {
            field = value
            positions = loadData(value)
        }

    private fun loadData(file: String): List<Vector2> {
        val frame = Rectangle(0.0, 0.0,1080.0, 1080.0)
        val pos = csvReader().readAllWithHeader(File(file)).map {
            Vector2(it["0"]!!.toDouble(), it["1"]!!.toDouble())
        }
        println("number of rows ${pos.size}")
        val mappedPositions = pos.map(pos.bounds, frame)
        pointIndices = mappedPositions.indices.map { Pair(mappedPositions[it], it) }.associate { it }
        kdtree = mappedPositions.kdTree()

        return mappedPositions
    }

    var positions = loadData(positionsFile)
        set(value) {
            field = value
            positionInstances.put {
                for (pos in value) {
                    write(pos)
                }
            }
        }
    var sizes = positions.map { 1.0 }
        set(value) {
            field = value
            sizeInstances.put {
                for (size in value) {
                    write(size.toFloat())
                }
            }
        }
    var colors = positions.map { ColorRGBa.WHITE.opacify(0.0) }
        set(value) {
            field = value
            colorsInstances.put {
                for (c in value) {
                    write(Vector4(c.r, c.g, c.b, c.alpha))
                }
            }
        }

    var kdtree = positions.kdTree()
    var pointIndices = positions.indices.map { Pair(positions[it], it) }.associate { it }
    var memes = loadMemes("datasets/memes-all.json").apply {
        println("number of memes: ${this.size}")
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

    private val positionInstances = vertexBuffer(vertexFormat {
        attribute("offset", VertexElementType.VECTOR2_FLOAT32)
    }, memes.size).also {
        it.put {
            for (pos in positions) {
                write(pos)
            }
        }
    }
    private val sizeInstances = vertexBuffer(vertexFormat {
        attribute("size", VertexElementType.FLOAT32)
    }, positions.size).also {
        it.put {
            for (j in positions.indices) {
                write(1.0f)
            }
        }
    }
    private val colorsInstances = vertexBuffer(vertexFormat {
        attribute("color", VertexElementType.VECTOR4_FLOAT32)
    }, positions.size).also {
        it.put {
            for (j in positions.indices) {
                write(Vector4.ZERO)
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
           
            
            x_fill = mix(texture(p_texture, vec2(tx, ty)), vi_color.rgba, vi_color.a);
        """.trimIndent()

        parameter("texture", memeImage)
        parameter("tileSize", 64.0)
        parameter("scale", imageScale.coerceAtLeast(1.0))
        parameter("inRadius", IntArray(1))
    }

    var currentIndexes = listOf<Int>()
        set(value) {
            if(field != value) {
                val nmemes = value.map { memes[it] }
                field = value
                plotterChange.trigger(nmemes)
            }
        }

    var plotterChange = Event<List<Meme>>()

    fun draw(drawer: Drawer, mousePos: Vector2 = Vector2.INFINITY) {
        drawer.isolated {

            drawer.shadeStyle = style
            drawer.vertexBufferInstances(
                listOf(quad),
                listOf(positionInstances, sizeInstances, colorsInstances),
                DrawPrimitive.TRIANGLE_STRIP,
                positionInstances.vertexCount
            )


            if(mousePos != Vector2.INFINITY) {
                val cursorPosition = (drawer.view.inversed * mousePos.xy01).div.xy
                val closest = kdtree.findAllInRadius(cursorPosition, 10.0)
                closest?.let {
                    val indexOfClosestOnes = pointIndices.filter { closest.contains(it.key) && sizes[it.value] != 0.0 }.map { it.value }
                    currentIndexes = indexOfClosestOnes
                }
            }



        }
    }
}