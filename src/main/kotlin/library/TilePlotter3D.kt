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

class MemePlotter3D(imageScale: Double = 10.0, var queryRadius: Double = 10.0, positions: List<Vector3> = listOf()) {
    val memeImage = loadImage("datasets/tiles-64.png")
    var memes = loadMemes("datasets/memes-all.json")

    private val positionInstances = vertexBuffer(vertexFormat {
        attribute("offset", VertexElementType.VECTOR3_FLOAT32)
    }, memes.size).also {
        if(positions.isNotEmpty()) {
            it.put {
                for (pos in positions) {
                    write(pos)
                }
            }
        }
    }
    private val sizeInstances = vertexBuffer(vertexFormat {
        attribute("size", VertexElementType.FLOAT32)
    }, memes.size).also {
        it.put {
            for (j in memes.indices) {
                write(1.0f)
            }
        }
    }
    private val colorsInstances = vertexBuffer(vertexFormat {
        attribute("color", VertexElementType.VECTOR4_FLOAT32)
    }, memes.size).also {
        it.put {
            for (j in memes.indices) {
                write(Vector4.ZERO)
            }
        }
    }


    var positions = List(memes.size) {Vector3.ZERO}
        set(value) {
            field = value
            if(positions.isNotEmpty()) {
                pointIndices = value.indices.map { Pair(value[it], it) }.associate { it }
                kdtree = value.kdTree()
                positionInstances.put {
                    for (pos in value) {
                        write(pos)
                    }
                }
            }
        }
    var sizes = memes.map { 1.0 }
        set(value) {
            field = value
            sizeInstances.put {
                for (size in value) {
                    write(size.toFloat())
                }
            }
        }
    var colors = memes.map { ColorRGBa.WHITE.opacify(0.0) }
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

    val quad = vertexBuffer(vertexFormat {
        position(3)
        textureCoordinate(2)
    }, 4).also {
        it.put {
            write(Vector3(-1.0, -1.0, 0.0))
            write(Vector2(0.0, 1.0))
            write(Vector3(1.0, -1.0, 0.0))
            write(Vector2(1.0, 1.0))
            write(Vector3(-1.0, 1.0, 0.0))
            write(Vector2(0.0, 0.0))
            write(Vector3(1.0, 1.0, 0.0))
            write(Vector2(1.0, 0.0))
        }
    }

    val style = shadeStyle {
        vertexTransform = """
            
            
            vec3 voffset = (x_viewMatrix * vec4(i_offset, 1.0)).xyz;
            x_viewMatrix = mat4(1.0, 0.0, 0.0, 0.0, 
                                0.0, 1.0, 0.0, 0.0, 
                                0.0, 0.0, 1.0, 0.0, 
                                0.0, 0.0, 0.0, 1.0);

            x_position.xy *= p_scale * i_size;
            x_position.xyz += voffset;
        """.trimIndent()

        fragmentTransform = """
            float itemsPerRow = textureSize(p_texture,0).x / p_tileSize;
            float tx = mod((c_instance * 1.0) / itemsPerRow, 1.0) + va_texCoord0.x/itemsPerRow; 
            float ty = floor(c_instance / itemsPerRow)/itemsPerRow + va_texCoord0.y / itemsPerRow;
            ty = 1.0 - ty;
           
           
            
            x_fill = mix(texture(p_texture, vec2(tx, ty)), vi_color.rgba, vi_color.a);
            if (x_fill.a < 0.9) discard;
        """.trimIndent()

        parameter("texture", memeImage)
        parameter("tileSize", 64.0)
        parameter("scale", imageScale.coerceAtLeast(1.0) / 10.0)
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


/*            if(mousePos != Vector2.INFINITY) {
                val cursorPosition = (drawer.view.inversed * mousePos.xy01).div.xy
                val closest = kdtree.findAllInRadius(cursorPosition, queryRadius)
                if(closest.isNotEmpty()) {
                    val indexOfClosestOnes = pointIndices.filter { closest.contains(it.key) && sizes[it.value] != 0.0 }.map { it.value }
                    currentIndexes = indexOfClosestOnes
                }

            }*/



        }
    }
}