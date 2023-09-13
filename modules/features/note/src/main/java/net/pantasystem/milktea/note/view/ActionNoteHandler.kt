package net.pantasystem.milktea.note.view

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.report.ReportDialog
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.dialog.ConfirmDeleteAndEditNoteDialog
import net.pantasystem.milktea.note.dialog.ConfirmDeleteNoteDialog
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

        mNotesViewModel.confirmDeletionEvent.filterNotNull().onEach { note ->
            if (activity.supportFragmentManager.findFragmentByTag(ConfirmDeleteNoteDialog.FRAGMENT_TAG) == null) {
                Log.d("ActionNoteHandler", "confirmDeletionEvent: ${note.note.id}")
                ConfirmDeleteNoteDialog.newInstance(note.note.id)
                    .show(activity.supportFragmentManager, ConfirmDeleteNoteDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(
            activity.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(activity.lifecycleScope)

        mNotesViewModel.confirmDeleteAndEditEvent.filterNotNull().onEach { note ->
            if (activity.supportFragmentManager.findFragmentByTag(ConfirmDeleteAndEditNoteDialog.FRAGMENT_TAG) == null) {
                ConfirmDeleteAndEditNoteDialog
                    .newInstance(note.note.id)
                    .show(activity.supportFragmentManager, ConfirmDeleteAndEditNoteDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(
            activity.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(activity.lifecycleScope)


        mNotesViewModel.confirmReportEvent.onEach { report ->
            report?.let {
                ReportDialog.newInstance(report.userId, report.comment, report.noteIds)
                    .show(activity.supportFragmentManager, ReportDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(activity.lifecycleScope)

    }
}