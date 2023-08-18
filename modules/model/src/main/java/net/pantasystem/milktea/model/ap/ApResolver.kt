package net.pantasystem.milktea.model.ap

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User

sealed interface ApResolver {
    data class TypeUser(val user: User) : ApResolver
    data class TypeNote(val note: Note) : ApResolver
}