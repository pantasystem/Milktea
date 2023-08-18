package net.pantasystem.milktea.note.view

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.confirm.ConfirmDialog
import net.pantasystem.milktea.common_android_ui.report.ReportDialog
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.confirm.ConfirmCommand
import net.pantasystem.milktea.model.confirm.ResultType
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel


class ActionNoteHandler(
    val activity: AppCompatActivity,
    private val mNotesViewModel: NotesViewModel,
    val confirmViewModel: ConfirmViewModel,
    val settingStore: SettingStore

) {

    fun initViewModelListener() {

        mNotesViewModel.statusMessage.onEach {
            Toast.makeText(activity, it.getString(activity), Toast.LENGTH_LONG).show()
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        mNotesViewModel.quoteRenoteTarget.onEach {
            val intent = NoteEditorActivity.newBundle(activity, quoteTo = it.id)
            activity.startActivity(intent)
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        mNotesViewModel.openNoteEditorEvent.filterNotNull().onEach { note ->
            activity.startActivity(
                NoteEditorActivity.newBundle(
                    activity,
                    draftNoteId = note.draftNoteId
                )
            )
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)


        mNotesViewModel.confirmDeletionEvent.filterNotNull().onEach {
            confirmViewModel.confirmEvent.tryEmit(
                ConfirmCommand(
                    activity.getString(R.string.confirm_deletion),
                    null,
                    eventType = "delete_note",
                    args = it.note
                )
            )
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        mNotesViewModel.confirmDeleteAndEditEvent.filterNotNull().onEach {
            confirmViewModel.confirmEvent.tryEmit(
                ConfirmCommand(
                    null,
                    activity.getString(R.string.confirm_delete_and_edit_note_description),
                    eventType = "delete_and_edit_note",
                    args = it
                )
            )
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        mNotesViewModel.confirmReportEvent.onEach { report ->
            report?.let {
                ReportDialog.newInstance(report.userId, report.comment, report.noteIds)
                    .show(activity.supportFragmentManager, "")
            }
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        confirmViewModel.confirmEvent.onEach {
            ConfirmDialog.newInstance(it).show(activity.supportFragmentManager, "")
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

        confirmViewModel.confirmedEvent.onEach {
            if (it.resultType == ResultType.NEGATIVE) {
                return@onEach
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
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

    }
}