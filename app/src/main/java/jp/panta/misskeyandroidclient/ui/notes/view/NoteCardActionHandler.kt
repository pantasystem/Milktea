package jp.panta.misskeyandroidclient.ui.notes.view

import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel

class NoteCardActionHandler(
    val notesViewModel: NotesViewModel,
) {

    fun onAction(action: NoteCardAction) {
        when (action) {
            is NoteCardAction.OnNoteCardClicked -> {
                notesViewModel.setShowNote(action.note)
            }
            is NoteCardAction.OnOptionButtonClicked -> {
                notesViewModel.setTargetToShare(action.note)
            }
            is NoteCardAction.OnPollChoiceClicked -> {
                notesViewModel.vote(
                    action.noteId,
                    action.poll,
                    action.choice,
                )
            }
            is NoteCardAction.OnReactionButtonClicked -> {
                notesViewModel.setTargetToReaction(action.note)
            }
            is NoteCardAction.OnReactionClicked -> {
                notesViewModel.postReaction(action.note, action.reaction)
            }
            is NoteCardAction.OnReactionLongClicked -> {
                notesViewModel.setShowReactionHistoryDialog(
                    action.note.toShowNote.note.id,
                    action.reaction
                )
            }
            is NoteCardAction.OnRenoteButtonClicked -> {
                notesViewModel.setTargetToReNote(action.note)
            }
            is NoteCardAction.OnRenoteButtonLongClicked -> {
                notesViewModel.showRenotes(action.note.toShowNote.note.id)
            }
            is NoteCardAction.OnReplyButtonClicked -> {
                notesViewModel.setTargetToReply(action.note)
            }
        }
    }
}