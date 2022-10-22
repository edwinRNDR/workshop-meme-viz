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
        val list = mutableListOf(id, name)

        if(nsfw) { list.add("nsfw") }
        if(year != null) { list.add(year.toString()) }
        if(origin != null) { list.add(origin) }
        if(status != null) { list.add(status) }
        if(badges != null) { list.add(badges) }
        if(tags.isNotEmpty()) { list.add(tags.concat()) }
        if(imageUrl != null) { list.add(imageUrl) }
        if(favorites != null) { list.add(favorites.toString()) }
        if(views != null) { list.add(views.toString()) }
        if(about != null) { list.add(about) }

        return list
    }
}

fun List<String>.concat() = this.joinToString(", ") { it }.takeWhile { it.isDigit() }

fun loadMemes(filename: String): List<Meme> {
    val a = Gson().fromJson(File(filename).readText(), Array<Meme>::class.java)
    return a.toList()
}

