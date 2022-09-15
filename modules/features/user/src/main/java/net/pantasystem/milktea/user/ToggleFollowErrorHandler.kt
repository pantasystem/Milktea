package net.pantasystem.milktea.user

import android.view.View
import com.google.android.material.snackbar.Snackbar
import net.pantasystem.milktea.model.user.User

class ToggleFollowErrorHandler(
    val view: View,
    val onRetry: (User.Id) -> Unit,
) {

    operator fun invoke(errorUiState: ToggleFollowErrorUiState?) {
        if (errorUiState == null) {
            return
        }
        Snackbar.make(view, R.string.failure, Snackbar.LENGTH_SHORT)
            .setAction(R.string.retry) {
                onRetry.invoke(errorUiState.userId)
            }
            .show()
    }
}