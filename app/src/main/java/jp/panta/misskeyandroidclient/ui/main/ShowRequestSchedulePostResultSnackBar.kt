package jp.panta.misskeyandroidclient.ui.main

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.Snackbar
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.notes.draft.DraftNoteSavedEvent

class ShowRequestSchedulePostResultSnackBar(
    private val activity: Activity,
    private val view: View
) {

    operator fun invoke(event: DraftNoteSavedEvent) {
        when (event) {
            is DraftNoteSavedEvent.Failed -> Unit
            is DraftNoteSavedEvent.Success -> {
                if (event.draftNote.reservationPostingAt != null) {
                    Snackbar.make(
                        view,
                        activity.getString(R.string.successfully_created_schedule_note),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}