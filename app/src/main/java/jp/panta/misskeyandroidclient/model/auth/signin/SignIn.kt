package jp.panta.misskeyandroidclient.model.auth.signin

data class SignIn(
    val username: String,
    val password: String,
    val token: String?
)