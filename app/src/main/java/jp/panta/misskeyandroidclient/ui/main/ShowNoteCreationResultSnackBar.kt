package jp.panta.misskeyandroidclient.ui.main

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar
import net.pantasystem.milktea.note.NoteDetailActivity
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.model.TaskState
import net.pantasystem.milktea.model.notes.Note

internal class ShowNoteCreationResultSnackBar(
    private val activity: Activity,
    private val noteTaskExecutor: CreateNoteTaskExecutor,
    private val view: View
) {

    operator fun invoke(taskState: TaskState<Note>) {
        when (taskState) {
            is TaskState.Error -> {
                activity.getString(R.string.note_creation_failure).showSnackBar(
                    activity.getString(R.string.retry) to ({
                        noteTaskExecutor.dispatch(taskState.task)
                    })
                )
            }
            is TaskState.Success -> {
                activity.getString(R.string.successfully_created_note).showSnackBar(
                    activity.getString(R.string.show) to ({
                        activity.startActivity(
                            NoteDetailActivity.newIntent(activity, taskState.res.id)
                        )
                    })
                )
            }
            is TaskState.Executing -> {
            }
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