package jp.panta.misskeyandroidclient.ui.main

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.worker.note.CreateNoteWorker
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor

internal class NoteCreateResultHandler(
    private val activity: AppCompatActivity,
    private val view: View,
    private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
) {

    operator fun invoke(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> Unit
            WorkInfo.State.RUNNING -> Unit
            WorkInfo.State.SUCCEEDED -> {
                val noteId =
                    workInfo.outputData.getLong(CreateNoteWorker.EXTRA_ACCOUNT_ID, -1).takeIf {
                        it != -1L
                    }?.let {
                        Note.Id(
                            it,
                            workInfo.outputData.getString(CreateNoteWorker.EXTRA_NOTE_ID) ?: ""
                        )
                    } ?: return
                activity.getString(R.string.successfully_created_note).showSnackBar(
                    activity.getString(R.string.show) to ({
                        activity.startActivity(
                            NoteDetailActivity.newIntent(activity, noteId)
                        )
                    })
                )
                createNoteWorkerExecutor.onHandled(workInfo.id)
            }

            WorkInfo.State.FAILED -> {
                val draftNoteId =
                    workInfo.outputData.getLong(CreateNoteWorker.EXTRA_DRAFT_NOTE_ID, -1).takeIf {
                        it != -1L
                    } ?: return
                if (activity.supportFragmentManager.findFragmentByTag("NotePostFailedDialogFragment") == null) {
                    val reasonType =
                        workInfo.outputData.getString(CreateNoteWorker.EXTRA_FAILED_REASON)
                    NotePostFailedDialogFragment.newInstance(
                        draftNoteId,
                        reasonType?.let {
                            CreateNoteWorker.ErrorReasonType.values().find {
                                it.name == reasonType
                            }
                        } ?: CreateNoteWorker.ErrorReasonType.UnknownError,
                        workInfo.outputData.getString(CreateNoteWorker.EXTRA_FAILED_STACKTRACE),
                    ).show(activity.supportFragmentManager, "NotePostFailedDialogFragment")
                }
                createNoteWorkerExecutor.onHandled(workInfo.id)
            }
            WorkInfo.State.BLOCKED -> Unit
            WorkInfo.State.CANCELLED -> Unit
        }
    }

    private fun String.showSnackBar(action: Pair<String, (View) -> Unit>? = null) {
        val snackBar =
            Snackbar.make(view, this, Snackbar.LENGTH_LONG)
        if (action != null) {
            snackBar.setAction(action.first, action.second)
        }
        snackBar.show()
    }


}