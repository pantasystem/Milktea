package jp.panta.misskeyandroidclient.ui.notes.view

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.ui.confirm.ConfirmDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.ReactionSelectionDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.RemoteReactionEmojiSuggestionDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices.ReactionInputDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.history.ReactionHistoryPagerDialog
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.picker.ReactionPickerDialog
import jp.panta.misskeyandroidclient.ui.notes.view.renote.RenotesBottomSheetDialog
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.SelectedReaction
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.media.MediaViewData
import jp.panta.misskeyandroidclient.ui.users.ReportDialog
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.file.FileViewData
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmCommand
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmEvent
import net.pantasystem.milktea.data.infrastructure.confirm.ResultType
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.setting.ReactionPickerType
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.report.Report


class ActionNoteHandler(
    val activity: AppCompatActivity,
    val mNotesViewModel: NotesViewModel,
    val confirmViewModel: ConfirmViewModel,
    val settingStore: SettingStore,

) {

    private val replyTargetObserver = Observer<PlaneNoteViewData> {
        activity.startActivity(
            NoteEditorActivity.newBundle(
                activity,
                replyTo = it.toShowNote.note.id
            )
        )
    }

    private val reNoteTargetObserver = Observer<PlaneNoteViewData> {
        Log.d("MainActivity", "renote clicked :$it")
        val dialog = RenoteBottomSheetDialog()
        dialog.show(activity.supportFragmentManager, "timelineFragment")

    }
    private val shareTargetObserver = Observer<PlaneNoteViewData> {
        Log.d("MainActivity", "share clicked :$it")
        ShareBottomSheetDialog().show(activity.supportFragmentManager, "MainActivity")
    }
    private val targetUserObserver = Observer<User> {
        Log.d("MainActivity", "user clicked :$it")
        val intent = UserDetailActivity.newInstance(activity, userId = it.id)

        intent.putActivity(Activities.ACTIVITY_IN_APP)


        activity.startActivity(intent)
    }

    private val statusMessageObserver = Observer<String> {
        Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
    }

    private val quoteRenoteTargetObserver = Observer<PlaneNoteViewData> {
        val intent = NoteEditorActivity.newBundle(activity, quoteTo = it.toShowNote.note.id)
        activity.startActivity(intent)
    }

    private val reactionTargetObserver = Observer<PlaneNoteViewData> {
        Log.d("MainActivity", "リアクションの対象ノートを選択:${it.toShowNote}")
        when (settingStore.reactionPickerType) {
            ReactionPickerType.LIST -> {
                ReactionSelectionDialog().show(activity.supportFragmentManager, "MainActivity")
            }
            ReactionPickerType.SIMPLE -> {
                ReactionPickerDialog().show(activity.supportFragmentManager, "Activity")
            }
        }
    }


    private val showNoteEventObserver = Observer<Note> {
        activity.startActivity(NoteDetailActivity.newIntent(activity, noteId = it.id))
    }
    private val fileTargetObserver = Observer<Pair<FileViewData, MediaViewData>> {
        Log.d("ActionNoteHandler", "${it.first.file}")
        val list = it.second.files.value!!.map { fv ->
            fv.file
        }
        val index = it.second.files.value!!.indexOfFirst { fv ->
            fv.file == it.first.file
        }
        val intent = net.pantasystem.milktea.media.MediaActivity.newInstance(activity, list, index)
        activity.startActivity(intent)
        //val intent =
    }

    private val reactionInputObserver = Observer<Unit> {
        val dialog = ReactionInputDialog()
        dialog.show(activity.supportFragmentManager, "")
    }

    private val openNoteEditor = Observer<DraftNote?> { note ->
        activity.startActivity(NoteEditorActivity.newBundle(activity, draftNote = note))
    }

    private val confirmDeletionEventObserver = Observer<PlaneNoteViewData> {
        confirmViewModel.confirmEvent.event = ConfirmCommand(
            activity.getString(R.string.confirm_deletion),
            null,
            eventType = "delete_note",
            args = it.toShowNote.note
        )
    }

    private val confirmDeleteAndEditEventObserver = Observer<PlaneNoteViewData> {
        confirmViewModel.confirmEvent.event = ConfirmCommand(
            null,
            activity.getString(R.string.confirm_delete_and_edit_note_description),
            eventType = "delete_and_edit_note",
            args = it.toShowNote
        )
    }

    private val confirmCommandEventObserver = Observer<ConfirmCommand> {
        ConfirmDialog().show(activity.supportFragmentManager, "")
    }

    private val confirmedEventObserver = Observer<ConfirmEvent> {
        if (it.resultType == ResultType.NEGATIVE) {
            return@Observer
        }
        when (it.eventType) {
            "delete_note" -> {
                if (it.args is Note) {
                    mNotesViewModel.removeNote((it.args as Note).id)
                }
            }
            "delete_and_edit_note" -> {
                if (it.args is NoteRelation) {
                    mNotesViewModel.removeAndEditNote(it.args as NoteRelation)
                }
            }
        }
    }

    private val showReactionHistoryDialogObserver: (ReactionHistoryRequest?) -> Unit = { req ->
        req?.let {
            ReactionHistoryPagerDialog.newInstance(req.noteId, it.type)
                .show(activity.supportFragmentManager, "")
        }
    }

    private val showRenotesDialogObserver: (Note.Id?) -> Unit = { id ->
        id?.let {
            RenotesBottomSheetDialog.newInstance(id).show(activity.supportFragmentManager, "")
        }
    }


    private val reportDialogObserver: (Report?) -> Unit = { report ->
        report?.let {
            ReportDialog.newInstance(report.userId, report.comment)
                .show(activity.supportFragmentManager, "")
        }
    }

    private val showRemoteReactionEmojiSuggestionDialogObserver: (SelectedReaction?) -> Unit = { reaction ->
        if (reaction != null) {
            RemoteReactionEmojiSuggestionDialog.newInstance(
                accountId = reaction.noteId.accountId,
                noteId = reaction.noteId.noteId,
                reaction = reaction.reaction
            ).show(activity.supportFragmentManager, "")
        }
    }

    fun initViewModelListener() {
        mNotesViewModel.replyTarget.removeObserver(replyTargetObserver)
        mNotesViewModel.replyTarget.observe(activity, replyTargetObserver)

        mNotesViewModel.reNoteTarget.removeObserver(reNoteTargetObserver)
        mNotesViewModel.reNoteTarget.observe(activity, reNoteTargetObserver)

        mNotesViewModel.shareTarget.removeObserver(shareTargetObserver)
        mNotesViewModel.shareTarget.observe(activity, shareTargetObserver)

        mNotesViewModel.targetUser.removeObserver(targetUserObserver)
        mNotesViewModel.targetUser.observe(activity, targetUserObserver)

        mNotesViewModel.statusMessage.removeObserver(statusMessageObserver)
        mNotesViewModel.statusMessage.observe(activity, statusMessageObserver)

        mNotesViewModel.quoteRenoteTarget.removeObserver(quoteRenoteTargetObserver)
        mNotesViewModel.quoteRenoteTarget.observe(activity, quoteRenoteTargetObserver)

        mNotesViewModel.reactionTarget.removeObserver(reactionTargetObserver)
        mNotesViewModel.reactionTarget.observe(activity, reactionTargetObserver)

        mNotesViewModel.targetFile.removeObserver(fileTargetObserver)
        mNotesViewModel.targetFile.observe(activity, fileTargetObserver)

        mNotesViewModel.showInputReactionEvent.removeObserver(reactionInputObserver)
        mNotesViewModel.showInputReactionEvent.observe(activity, reactionInputObserver)

        mNotesViewModel.openNoteEditor.removeObserver(openNoteEditor)
        mNotesViewModel.openNoteEditor.observe(activity, openNoteEditor)

        mNotesViewModel.showNoteEvent.removeObserver(showNoteEventObserver)
        mNotesViewModel.showNoteEvent.observe(activity, showNoteEventObserver)

        mNotesViewModel.confirmDeletionEvent.removeObserver(confirmDeletionEventObserver)
        mNotesViewModel.confirmDeletionEvent.observe(activity, confirmDeletionEventObserver)

        mNotesViewModel.confirmDeleteAndEditEvent.removeObserver(confirmDeleteAndEditEventObserver)
        mNotesViewModel.confirmDeleteAndEditEvent.observe(
            activity,
            confirmDeleteAndEditEventObserver
        )

        confirmViewModel.confirmEvent.removeObserver(confirmCommandEventObserver)
        confirmViewModel.confirmEvent.observe(activity, confirmCommandEventObserver)

        confirmViewModel.confirmedEvent.removeObserver(confirmedEventObserver)
        confirmViewModel.confirmedEvent.observe(activity, confirmedEventObserver)

        mNotesViewModel.showReactionHistoryEvent.removeObserver(showReactionHistoryDialogObserver)
        mNotesViewModel.showReactionHistoryEvent.observe(
            activity,
            showReactionHistoryDialogObserver
        )

        mNotesViewModel.showRenotesEvent.removeObserver(showRenotesDialogObserver)
        mNotesViewModel.showRenotesEvent.observe(activity, showRenotesDialogObserver)

        mNotesViewModel.confirmReportEvent.removeObserver(reportDialogObserver)
        mNotesViewModel.confirmReportEvent.observe(activity, reportDialogObserver)

        mNotesViewModel.showRemoteReactionEmojiSuggestionDialog.removeObserver(showRemoteReactionEmojiSuggestionDialogObserver)
        mNotesViewModel.showRemoteReactionEmojiSuggestionDialog.observe(activity, showRemoteReactionEmojiSuggestionDialogObserver)
    }
}