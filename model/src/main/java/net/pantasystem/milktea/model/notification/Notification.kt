package net.pantasystem.milktea.model.notification

import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.notes.Note
import kotlinx.datetime.Instant


sealed class Notification {
    abstract val id: Id
    abstract val createdAt: Instant
    abstract val isRead: Boolean

    abstract fun read(): Notification

    data class Id(
        val accountId: Long,
        val notificationId: String
    ) : EntityId
}


interface HasNote {
    val noteId: Note.Id
}

interface HasUser {
    val userId: net.pantasystem.milktea.model.user.User.Id
}

data class FollowNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val isRead: Boolean
) : Notification(), HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class FollowRequestAcceptedNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val isRead: Boolean

) : Notification(), HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class ReceiveFollowRequestNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val isRead: Boolean

) : Notification(), HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class MentionNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}


data class ReplyNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class RenoteNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class QuoteNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class ReactionNotification(
    override val id: Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    override val noteId: Note.Id,
    val reaction: String,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class PollVoteNotification(
    override val id: Id,
    override val noteId: Note.Id,

    override val createdAt: Instant,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    val choice: Int,
    override val isRead: Boolean

) : Notification(), HasNote, HasUser {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class PollEndedNotification(
    override val id: Id,
    override val createdAt: Instant,
    override val isRead: Boolean,
    override val noteId: Note.Id,

    ) : Notification(), HasNote {
    override fun read(): Notification {
        return copy(isRead = true)
    }
}

data class UnknownNotification(
    override val id: Id,
    override val createdAt: Instant,
    override val isRead: Boolean,
    override val userId: net.pantasystem.milktea.model.user.User.Id,
    val rawType: String

) : Notification(), HasUser {
    override fun read(): Notification {
        return this.copy(isRead = true)
    }
}