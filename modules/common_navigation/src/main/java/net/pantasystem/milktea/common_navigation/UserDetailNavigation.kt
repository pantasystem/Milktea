package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.user.User

interface UserDetailNavigation : ActivityNavigation<UserDetailNavigationArgs>

sealed interface UserDetailNavigationArgs {
    data class UserId(val userId: User.Id): UserDetailNavigationArgs
    data class UserName(val userName: String): UserDetailNavigationArgs
}