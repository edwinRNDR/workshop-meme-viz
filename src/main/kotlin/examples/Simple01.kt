package examples

import library.loadMemes
import org.openrndr.application
import java.io.File

fun main() {
    application {
        program {
            val memes = loadMemes("datasets/memes-all.json")
            println("loaded ${memes.size} memes")
        }
    }
}