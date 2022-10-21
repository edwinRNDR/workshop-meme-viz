package library

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileReader

data class Meme(
    val id: String = "",
    val name: String = "",
    val nsfw: Boolean = false,
    val year: Int? = null,
    val origin: String? = null,
    val status: String? = null,
    val badges: String? = null,
    val tags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val favorites: Int? = null,
    val views: Int? = null,
    val about: String? = null
) {
    fun toList(): List<String> {
        return listOf(id, name, year.toString())
    }
}

fun loadMemes(filename: String): List<Meme> {
    val a = Gson().fromJson(File(filename).readText(), Array<Meme>::class.java)
    return a.toList()
}

