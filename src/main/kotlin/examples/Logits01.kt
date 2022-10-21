package examples

import library.loadLogits
import org.openrndr.application

fun main() = application {
    program {
        extend {
            val logits = loadLogits("datasets/mood-logits-all.json")
        }
    }
}