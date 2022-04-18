package net.pantasystem.milktea.data.infrastructure.auth.signin

data class SignIn(
    val username: String,
    val password: String,
    val token: String?
)