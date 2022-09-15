package net.pantasystem.milktea.common_navigation

import net.pantasystem.milktea.model.user.User

interface UserListNavigation : ActivityNavigation<UserListArgs>

data class UserListArgs(val userId: User.Id? = null)