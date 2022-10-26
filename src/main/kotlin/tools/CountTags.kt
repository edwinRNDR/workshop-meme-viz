import library.loadMemes

fun main() {
    val memes = loadMemes("datasets/memes-all.json")
    val count = mutableMapOf<String, Int>()

    for (meme in memes) {
        for (tag in meme.tags) {
            val c = count.getOrPut(tag) { 0 }
            count[tag] = c+1
        }
    }
    val top = count.toList().sortedByDescending { it.second }
    for (t in top) {
        println("${String.format("%5d", t.second)} ${t.first}")
    }
}