package net.pantasystem.milktea.note.view

import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.reaction.ReactionCountAction
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class NoteCardActionListenerAdapter(
    val onAction: (NoteCardAction) -> Unit,
) {

    fun onReplyButtonClicked(note: PlaneNoteViewData) {
        onAction(NoteCardAction.OnReplyButtonClicked(note))
    }

    fun onRenoteButtonClicked(note: PlaneNoteViewData) {
        onAction(NoteCardAction.OnRenoteButtonClicked(note))
    }

    fun onOptionButtonClicked(note: PlaneNoteViewData) {
        onAction(NoteCardAction.OnOptionButtonClicked(note))
    }

    fun onReactionButtonClicked(note: PlaneNoteViewData) {
        onAction(NoteCardAction.OnReactionButtonClicked(note))
    }

    fun onUserClicked(user: User?) {
        if (user != null) {
            onAction(NoteCardAction.OnUserClicked(user))
        }
    }

    fun onPollChoiceClicked(noteId: Note.Id, poll: Poll, choice: Poll.Choice) {
        onAction(NoteCardAction.OnPollChoiceClicked(noteId, poll, choice))
    }

    fun onRenoteButtonLongClicked(note: PlaneNoteViewData) {
        onAction(NoteCardAction.OnRenoteButtonLongClicked(note))
    }

    fun onNoteCardClicked(note: Note) {
        onAction(NoteCardAction.OnNoteCardClicked(note))
    }

    fun onReactionCountAction(action: ReactionCountAction) {
        when(action) {
            is ReactionCountAction.OnClicked -> {
                onAction(NoteCardAction.OnReactionClicked(action.note, action.reaction))
            }
            is ReactionCountAction.OnLongClicked -> {
                onAction(NoteCardAction.OnReactionLongClicked(action.note, action.reaction))
            }
        }
    }

    fun onFavoriteButtonClicked(note: Note) {
        onAction(NoteCardAction.OnFavoriteButtonClicked(note))
    }

    fun onChannelButtonClicked(channelId: Channel.Id) {
        onAction(NoteCardAction.OnChannelButtonClicked(channelId))
    }

}




sealed interface NoteCardAction {
    data class OnReplyButtonClicked(val note: PlaneNoteViewData) : NoteCardAction
    data class OnRenoteButtonClicked(val note: PlaneNoteViewData) : NoteCardAction
    data class OnOptionButtonClicked(val note: PlaneNoteViewData) : NoteCardAction
    data class OnReactionButtonClicked(val note: PlaneNoteViewData) : NoteCardAction
    data class OnReactionClicked(val note: PlaneNoteViewData, val reaction: String) : NoteCardAction
    data class OnReactionLongClicked(val note: PlaneNoteViewData, val reaction: String) : NoteCardAction
    data class OnPollChoiceClicked(val noteId: Note.Id, val poll: Poll, val choice: Poll.Choice) : NoteCardAction
    data class OnRenoteButtonLongClicked(val note: PlaneNoteViewData) : NoteCardAction
    data class OnNoteCardClicked(val note: Note) : NoteCardAction
    data class OnUserClicked(val user: User) : NoteCardAction
    data class OnFavoriteButtonClicked(val note: Note) : NoteCardAction
    data class OnChannelButtonClicked(val channelId: Channel.Id) : NoteCardAction
}