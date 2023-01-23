package net.pantasystem.milktea.model.search

data class SearchHistory(
    val accountId: Long,
    val keyword: String,
    val id: Long = 0L
)