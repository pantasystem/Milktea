package net.pantasystem.milktea.data.model.auth.signin

data class SignIn(
    val username: String,
    val password: String,
    val token: String?
)