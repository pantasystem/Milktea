package net.pantasystem.milktea.model.notes

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.user.User

fun generateEmptyNote(): Note {
    return Note(
        id = Note.Id(0L, "id1"),
        text = null,
        createdAt = Clock.System.now(),
        cw = null,
        userId = User.Id(0L, "id2"),
        replyId = null,
        renoteId = null,
        visibility = Visibility.Public(false),
        viaMobile = true,
        localOnly = null,
        visibleUserIds = null,
        url = null,
        uri = null,
        renoteCount = 0,
        reactionCounts = emptyList(),
        emojis = null,
        repliesCount = 0,
        poll = null,
        myReaction = null,
        app = null,
        channelId = null,
        fileIds = null,
        type = Note.Type.Misskey,
    )
}