package net.pantasystem.milktea.model.hashtag

data class HashTag(
    val name: String,
    val usesCount: Int,
    val chart: List<Int>,
)