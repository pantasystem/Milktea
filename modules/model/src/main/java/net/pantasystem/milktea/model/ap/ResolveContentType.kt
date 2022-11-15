package net.pantasystem.milktea.model.ap

import android.provider.ContactsContract.CommonDataKinds.Note
import net.pantasystem.milktea.model.user.User

sealed interface ResolveContentType {
    data class ContentUser(val user: User)
    data class ContentNote(val note: Note)
}