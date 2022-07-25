package jp.panta.misskeyandroidclient.ui.notes.view

import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.poll.Poll

class NoteCardActionListenerAdapter(
    val onAction: (NoteCardAction) -> Unit,
) {

    fun onReplyButtonClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnReplyButtonClicked(note))
    }

    fun onRenoteButtonClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnRenoteButtonClicked(note))
    }

    fun onOptionButtonClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnOptionButtonClicked(note))
    }

    fun onReactionButtonClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnReactionButtonClicked(note))
    }

    fun onReactionClicked(note: NoteRelation, reaction: String) {
        onAction(NoteCardAction.OnReactionClicked(note, reaction))
    }

    fun onReactionLongClicked(note: NoteRelation, reaction: String) {
        onAction(NoteCardAction.OnReactionLongClicked(note, reaction))
    }

    fun onPollChoiceClicked(note: NoteRelation, choice: Poll.Choice) {
        onAction(NoteCardAction.OnPollChoiceClicked(note, choice))
    }

    fun onRenoteButtonLongClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnRenoteButtonLongClicked(note))
    }

    fun onNoteCardClicked(note: NoteRelation) {
        onAction(NoteCardAction.OnNoteCardClicked(note))
    }

//    fun onReactionCountAction(action: ReactionCountAction) {
//        when(action) {
//            is ReactionCountAction.OnReactionClicked -> {
//                onAction(NoteCardAction.OnReactionClicked(action.note, action.reaction))
//            }
//            is ReactionCountAction.OnReactionLongClicked -> {
//                onAction(NoteCardAction.OnReactionLongClicked(action.note, action.reaction))
//            }
//        }
//    }

}




sealed interface NoteCardAction {
    val note: NoteRelation
    data class OnReplyButtonClicked(override val note: NoteRelation) : NoteCardAction
    data class OnRenoteButtonClicked(override val note: NoteRelation) : NoteCardAction
    data class OnOptionButtonClicked(override val note: NoteRelation) : NoteCardAction
    data class OnReactionButtonClicked(override val note: NoteRelation) : NoteCardAction
    data class OnReactionClicked(override val note: NoteRelation, val reaction: String) : NoteCardAction
    data class OnReactionLongClicked(override val note: NoteRelation, val reaction: String) : NoteCardAction
    data class OnPollChoiceClicked(override val note: NoteRelation, val choice: Poll.Choice) : NoteCardAction
    data class OnRenoteButtonLongClicked(override val note: NoteRelation) : NoteCardAction
    data class OnNoteCardClicked(override val note: NoteRelation) : NoteCardAction
}