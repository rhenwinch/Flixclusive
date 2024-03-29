package com.flixclusive.model.tmdb

import com.flixclusive.core.util.film.FilmType
import com.flixclusive.core.util.film.FilmType.Companion.toFilmType
import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
    override val id: Int = 0,
    override val title: String = "",
    val image: String? = null,
    val mediaType: String = "",
    override val language: String = "en",
    override val rating: Double = 0.0,
    val releaseDate: String = "",
    override val isReleased: Boolean = false,
) : Film, java.io.Serializable {
    override val posterImage: String?
        get() = image
    override val filmType: FilmType
        get() = mediaType.toFilmType()
    override val dateReleased: String
        get() = releaseDate
    override val genres: List<Genre>
        get() = emptyList()
    override val recommendedTitles: List<Recommendation>
        get() = emptyList()
}