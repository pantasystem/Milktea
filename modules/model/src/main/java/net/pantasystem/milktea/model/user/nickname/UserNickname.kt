package net.pantasystem.milktea.model.user.nickname

data class UserNickname(
    val id: Id,
    val name: String,

    ) {
    data class Id(
        val userName: String,
        val host: String
    )
}