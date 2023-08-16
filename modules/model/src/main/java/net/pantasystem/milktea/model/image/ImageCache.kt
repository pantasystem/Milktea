package net.pantasystem.milktea.model.image

import kotlinx.datetime.Instant

data class ImageCache(
    val sourceUrl: String,
    val cachePath: String,
    val cachedAt: Instant,
    val width: Int?,
    val height: Int?,
)