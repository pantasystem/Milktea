package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.account.Account


interface AuthorizationNavigation : ActivityNavigation<AuthorizationArgs>

sealed interface AuthorizationArgs {
    object New : AuthorizationArgs
    data class ReAuth(val account: Account?) : AuthorizationArgs
}