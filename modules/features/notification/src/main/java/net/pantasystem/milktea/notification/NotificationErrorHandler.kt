package net.pantasystem.milktea.notification

import android.content.Context
import android.widget.Toast
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter

class NotificationErrorHandler(
    val context: Context
) {

    operator fun invoke(error: Throwable) {
        when (error) {
            is APIError -> {
                Toast.makeText(
                    context,
                    APIErrorStringConverter()(error).getString(context),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> Unit
        }
    }
}