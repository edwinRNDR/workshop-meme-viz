package examples

import MemePlotter3D
import loadPositions
import org.openrndr.application
import org.openrndr.extra.camera.Orbital

fun main() = application {
    configure {
        width = 1080
        height = 1080
    }
    program {

        var positions = loadPositions("datasets/positions/bert-tsne.csv").map { it.vector3(z = Math.random()*10.0) }

        val tp = MemePlotter3D(5.0, 15.0, positions)


        extend(Orbital())
        extend {

            tp.draw(drawer)

        }
    }
}
