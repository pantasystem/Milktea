package net.pantasystem.milktea.model.user.report

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User

sealed interface ReportState {
    data class Specify(
        val userId: User.Id,
        val noteIds: List<Note.Id>,
        val comment: String
    ) : ReportState {
        val canSend: Boolean
            get() = this.comment.isNotBlank()
    }
    object None : ReportState


    sealed interface Sending : ReportState {
        val userId: User.Id
        val comment: String
        data class Doing(
            override val userId: User.Id,
            override val comment: String
        ) : Sending

        data class Failed(
            override val userId: User.Id,
            override val comment: String
        ) : Sending

        data class Success(
            override val userId: User.Id,
            override val comment: String
        ) : Sending
    }
}
