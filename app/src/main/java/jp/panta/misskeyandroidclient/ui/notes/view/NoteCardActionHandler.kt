package jp.panta.misskeyandroidclient.ui.notes.view

import androidx.appcompat.app.AppCompatActivity
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionSelectionDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.history.ReactionHistoryPagerDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.picker.ReactionPickerDialog
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.setting.ReactionPickerType

class NoteCardActionHandler(
    val activity: AppCompatActivity,
    val notesViewModel: NotesViewModel,
    val settingStore: SettingStore
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
                when (settingStore.reactionPickerType) {
                    ReactionPickerType.LIST -> {
                        ReactionSelectionDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, "MainActivity")
                    }
                    ReactionPickerType.SIMPLE -> {
                        ReactionPickerDialog.newInstance(action.note.toShowNote.note.id).show(activity.supportFragmentManager, "Activity")
                    }
                }
            }
            is NoteCardAction.OnReactionClicked -> {
                notesViewModel.postReaction(action.note, action.reaction)
            }
            is NoteCardAction.OnReactionLongClicked -> {
//                notesViewModel.setShowReactionHistoryDialog(
//                    action.note.toShowNote.note.id,
//                    action.reaction
//                )
                ReactionHistoryPagerDialog.newInstance(action.note.toShowNote.note.id, action.reaction)
                    .show(activity.supportFragmentManager, "")
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