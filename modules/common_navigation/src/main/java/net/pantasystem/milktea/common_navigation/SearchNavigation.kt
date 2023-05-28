package net.pantasystem.milktea.common_navigation

interface SearchNavigation : ActivityNavigation<SearchNavType>

sealed interface SearchNavType {
    val searchWord: String?
    val acct: String?
    val accountId: Long?

    data class ResultScreen(
        override val searchWord: String, override val acct: String? = null,
        override val accountId: Long? = null,
    ) : SearchNavType

    data class SearchScreen(
        override val searchWord: String? = null, override val acct: String? = null,
        override val accountId: Long? = null,
    ) : SearchNavType

}