package jp.panta.misskeyandroidclient.model.users.nickname

data class UserNickname(
    val id: Id,
    val name: String,

) {
    data class Id(
        val userName: String,
        val host: String
    )
}