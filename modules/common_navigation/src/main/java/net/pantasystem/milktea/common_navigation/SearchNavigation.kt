package net.pantasystem.milktea.common_navigation

interface SearchNavigation : ActivityNavigation<SearchNavArgs>

data class SearchNavArgs(
    val searchWord: String,
)