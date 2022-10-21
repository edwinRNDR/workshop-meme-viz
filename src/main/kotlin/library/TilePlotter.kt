import org.openrndr.draw.*
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3

class MemePlotter {
    val memeImage = loadImage("tiles-64.png")
    val positions = (0 until 6400).map { Vector2.uniform(0.0, 1024.0) }

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

    val instances = vertexBuffer(vertexFormat {
        attribute("offset", VertexElementType.VECTOR2_FLOAT32)
    }, positions.size).also {
        it.put {
            for (pos in positions) {
                write(pos)
            }
        }
    }

    val style = shadeStyle {
        vertexTransform = """
            x_position.xy *= 10.0;
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
    }

    fun draw(drawer: Drawer) {
        drawer.isolated {
            drawer.shadeStyle = style
            drawer.vertexBufferInstances(
                listOf(quad),
                listOf(instances),
                DrawPrimitive.TRIANGLE_STRIP,
                instances.vertexCount
            )
        }
    }
}