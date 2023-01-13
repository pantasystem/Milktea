package net.pantasystem.milktea.note.viewmodel

import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.R

object NoteStatusMessageTextGenerator {

    operator fun invoke(note: NoteRelation?, isUserNameDefault: Boolean): StringSource? {
        if (note == null) {
            return null
        }
        val name = if (isUserNameDefault) {
            note.user.displayUserName
        } else {
            note.user.displayName
        }
        return when {
            note.reply != null -> {
                StringSource(R.string.replied_by, name)
            }
            note.note.isRenote() && !note.note.hasContent() -> {
                StringSource(R.string.renoted_by, name)
            }
            note is NoteRelation.Featured -> {
                StringSource(R.string.featured)
            }
            note is NoteRelation.Promotion -> {
                StringSource(R.string.promotion)
            }

            else -> null
        }
    }
}