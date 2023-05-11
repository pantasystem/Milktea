package net.pantasystem.milktea.common_navigation

interface SearchNavigation : ActivityNavigation<SearchNavType>

sealed interface SearchNavType {
    val searchWord: String?
    val acct: String?
    data class ResultScreen(override val searchWord: String, override val acct: String? = null) : SearchNavType
    data class SearchScreen(override val searchWord: String? = null, override val acct: String? = null) : SearchNavType

}