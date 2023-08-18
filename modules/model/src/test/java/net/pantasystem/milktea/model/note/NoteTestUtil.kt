package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.model.user.User

fun generateEmptyNote(): Note {
    return Note.make(
        id = Note.Id(0L, "id1"),
        userId = User.Id(0L, "id2"),
    )
}