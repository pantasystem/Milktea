package net.pantasystem.milktea.model.hashtag

data class Hashtag(
    val name: String,
    val usesCount: Int,
    val chart: List<Int>,
)