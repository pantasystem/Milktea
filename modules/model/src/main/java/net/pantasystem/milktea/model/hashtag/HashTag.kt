package net.pantasystem.milktea.model.hashtag

data class HashTag(
    val name: String,
    val usersCount: Int,
    val chart: List<Int>,
)