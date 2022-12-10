package net.pantasystem.milktea.note.view

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.confirm.ConfirmDialog
import net.pantasystem.milktea.common_android_ui.report.ReportDialog
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.confirm.ConfirmCommand
import net.pantasystem.milktea.model.confirm.ConfirmEvent
import net.pantasystem.milktea.model.confirm.ResultType
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.user.report.Report
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel


class ActionNoteHandler(
    val activity: AppCompatActivity,
    private val mNotesViewModel: NotesViewModel,
    val confirmViewModel: ConfirmViewModel,
    val settingStore: SettingStore,

    ) {

    private val statusMessageObserver = Observer<String> {
        Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
    }

    private val quoteRenoteTargetObserver = Observer<Note> {
        val intent = NoteEditorActivity.newBundle(activity, quoteTo = it.id)
        activity.startActivity(intent)
    }



    private val openNoteEditor = Observer<DraftNote?> { note ->
        activity.startActivity(
            NoteEditorActivity.newBundle(
                activity,
                draftNoteId = note.draftNoteId
            )
        )
    }

    private val confirmDeletionEventObserver = Observer<NoteRelation> {
        confirmViewModel.confirmEvent.event = ConfirmCommand(
            activity.getString(R.string.confirm_deletion),
            null,
            eventType = "delete_note",
            args = it.note
        )
    }

    private val confirmDeleteAndEditEventObserver = Observer<NoteRelation> {
        confirmViewModel.confirmEvent.event = ConfirmCommand(
            null,
            activity.getString(R.string.confirm_delete_and_edit_note_description),
            eventType = "delete_and_edit_note",
            args = it
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

    private val reportDialogObserver: (Report?) -> Unit = { report ->
        report?.let {
            ReportDialog.newInstance(report.userId, report.comment)
                .show(activity.supportFragmentManager, "")
        }
    }


    fun initViewModelListener() {




        mNotesViewModel.statusMessage.removeObserver(statusMessageObserver)
        mNotesViewModel.statusMessage.observe(activity, statusMessageObserver)

        mNotesViewModel.quoteRenoteTarget.removeObserver(quoteRenoteTargetObserver)
        mNotesViewModel.quoteRenoteTarget.observe(activity, quoteRenoteTargetObserver)

        mNotesViewModel.openNoteEditor.removeObserver(openNoteEditor)
        mNotesViewModel.openNoteEditor.observe(activity, openNoteEditor)


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

        mNotesViewModel.confirmReportEvent.removeObserver(reportDialogObserver)
        mNotesViewModel.confirmReportEvent.observe(activity, reportDialogObserver)

    }
}