package jp.panta.misskeyandroidclient.ui.notes.view

import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionCountAction
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll

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

    fun onReactionClicked(note: PlaneNoteViewData, reaction: String) {
        onAction(NoteCardAction.OnReactionClicked(note, reaction))
    }

    fun onReactionLongClicked(note: PlaneNoteViewData, reaction: String) {
        onAction(NoteCardAction.OnReactionLongClicked(note, reaction))
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
}