package net.pantasystem.milktea.note.view

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common_android_ui.report.ReportDialog
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.dialog.ConfirmDeleteAndEditNoteDialog
import net.pantasystem.milktea.note.dialog.ConfirmDeleteNoteDialog
import net.pantasystem.milktea.note.viewmodel.NotesViewModel


class NoteActionHandler(
    private val fragmentManager: FragmentManager,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val notesViewModel: NotesViewModel
) {

    fun initViewModelListener() {

        notesViewModel.statusMessage.onEach {
            Toast.makeText(context, it.getString(context), Toast.LENGTH_LONG).show()
        }.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleOwner.lifecycleScope)

        notesViewModel.quoteRenoteTarget
            .onEach {
                val intent = NoteEditorActivity.newBundle(
                    context,
                    quoteTo = it.noteId,
                    channelId = it.channelId
                )
                context.startActivity(intent)
            }
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleOwner.lifecycleScope)

        notesViewModel.openNoteEditorEvent.filterNotNull().onEach { note ->
            context.startActivity(
                NoteEditorActivity.newBundle(
                    context,
                    draftNoteId = note.draftNoteId
                )
            )
        }.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleOwner.lifecycleScope)

        notesViewModel.confirmDeletionEvent.filterNotNull().onEach { note ->
            if (fragmentManager.findFragmentByTag(ConfirmDeleteNoteDialog.FRAGMENT_TAG) == null) {
                ConfirmDeleteNoteDialog.newInstance(note.note.id)
                    .show(fragmentManager, ConfirmDeleteNoteDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(lifecycleOwner.lifecycleScope)

        notesViewModel.confirmDeleteAndEditEvent.filterNotNull().onEach { note ->
            if (fragmentManager.findFragmentByTag(ConfirmDeleteAndEditNoteDialog.FRAGMENT_TAG) == null) {
                ConfirmDeleteAndEditNoteDialog
                    .newInstance(note.note.id)
                    .show(fragmentManager, ConfirmDeleteAndEditNoteDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(lifecycleOwner.lifecycleScope)


        notesViewModel.confirmReportEvent.onEach { report ->
            report?.let {
                ReportDialog.newInstance(report.userId, report.comment, report.noteIds)
                    .show(fragmentManager, ReportDialog.FRAGMENT_TAG)
            }
        }.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .launchIn(lifecycleOwner.lifecycleScope)

    }
}