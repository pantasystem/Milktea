package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import javax.inject.Inject

@AndroidEntryPoint
class NotePostFailedDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val DRAFT_NOTE_ID = "DRAFT_NOTE_ID"

        fun newInstance(draftNoteId: Long): NotePostFailedDialogFragment {
            return NotePostFailedDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(DRAFT_NOTE_ID, draftNoteId)
                }
            }
        }
    }


    @Inject
    internal lateinit var createNoteWorkerExecutor: CreateNoteWorkerExecutor

    private val draftNoteId: Long by lazy {
        requireArguments().getLong(DRAFT_NOTE_ID)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.note_creation_failure)
            .setPositiveButton(R.string.retry){ _, _ ->
                createNoteWorkerExecutor.enqueue(draftNoteId)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }
}