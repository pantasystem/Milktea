package jp.panta.misskeyandroidclient.ui.notes.view

import androidx.appcompat.app.AppCompatActivity
import jp.panta.misskeyandroidclient.NoteDetailActivity
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionSelectionDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.RemoteReactionEmojiSuggestionDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.history.ReactionHistoryPagerDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.picker.ReactionPickerDialog
import jp.panta.misskeyandroidclient.ui.notes.view.renote.RenotesBottomSheetDialog
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.setting.ReactionPickerType

class NoteCardActionHandler(
    val activity: AppCompatActivity,
    val notesViewModel: NotesViewModel,
    val settingStore: SettingStore
) {

    fun onAction(action: NoteCardAction) {
        when (action) {
            is NoteCardAction.OnNoteCardClicked -> {
                activity.startActivity(
                    NoteDetailActivity.newIntent(
                        activity,
                        noteId = action.note.id
                    )
                )
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
                        ReactionPickerDialog.newInstance(action.note.toShowNote.note.id)
                            .show(activity.supportFragmentManager, "Activity")
                    }
                }
            }
            is NoteCardAction.OnReactionClicked -> {
                if (!Reaction(action.reaction).isLocal()) {
                    RemoteReactionEmojiSuggestionDialog.newInstance(
                        accountId = action.note.id.accountId,
                        noteId = action.note.toShowNote.note.id.noteId,
                        reaction = action.reaction
                    ).show(activity.supportFragmentManager, "")
                    return
                }
                notesViewModel.postReaction(action.note, action.reaction)
            }
            is NoteCardAction.OnReactionLongClicked -> {
                ReactionHistoryPagerDialog.newInstance(
                    action.note.toShowNote.note.id,
                    action.reaction
                ).show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnRenoteButtonClicked -> {
                RenoteBottomSheetDialog.newInstance(
                    action.note.note.note.id,
                    action.note.isMyNote && action.note.note.note.isRenote()
                ).show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnRenoteButtonLongClicked -> {
                RenotesBottomSheetDialog.newInstance(action.note.toShowNote.note.id)
                    .show(activity.supportFragmentManager, "")
            }
            is NoteCardAction.OnReplyButtonClicked -> {
                activity.startActivity(
                    NoteEditorActivity.newBundle(
                        activity,
                        replyTo = action.note.toShowNote.note.id
                    )
                )
            }
        }
    }
}