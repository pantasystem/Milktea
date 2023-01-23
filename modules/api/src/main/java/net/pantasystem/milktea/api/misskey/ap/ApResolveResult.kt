package net.pantasystem.milktea.api.misskey.ap

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO

@kotlinx.serialization.Serializable
sealed class ApResolveResult {
    @SerialName("User")
    @kotlinx.serialization.Serializable
    data class TypeUser(
        @SerialName("object") val user: UserDTO
    ) : ApResolveResult()

    @kotlinx.serialization.Serializable
    @SerialName("Note")
    data class TypeNote(
        @SerialName("object") val note: NoteDTO
    ) : ApResolveResult()
}