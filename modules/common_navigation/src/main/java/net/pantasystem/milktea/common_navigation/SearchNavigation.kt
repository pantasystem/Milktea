package net.pantasystem.milktea.common_navigation

interface SearchNavigation : ActivityNavigation<SearchNavType>

sealed interface SearchNavType {
    val searchWord: String?
    data class ResultScreen(override val searchWord: String) : SearchNavType
    data class SearchScreen(override val searchWord: String? = null) : SearchNavType

}