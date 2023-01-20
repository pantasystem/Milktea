package net.pantasystem.milktea.api_streaming.mastodon

sealed interface ConnectTo {
    object LocalPublic : ConnectTo
    object Public : ConnectTo
    data class Hashtag(val tag: String) : ConnectTo
    data class LocalHashtag(val tag: String) : ConnectTo
    object User : ConnectTo
    data class UserList(val listId: String) : ConnectTo
}