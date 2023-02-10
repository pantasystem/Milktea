package net.pantasystem.milktea.note.timeline

import android.content.Context
import android.util.Log
import android.widget.Toast
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.note.R
import java.io.IOException

class TimelineErrorHandler(
    val context: Context,
) {

    operator fun invoke(error: Throwable) {
        Log.e("TimelineErrorHandler", "error", error)
        when (error) {
            is IOException -> {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError -> {
                Toast.makeText(context, APIErrorStringConverter()(error).getString(context), Toast.LENGTH_LONG)
                    .show()
            }

            is UnauthorizedException -> {
                Toast.makeText(
                    context,
                    R.string.unauthorized_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(context, "error:$error", Toast.LENGTH_SHORT).show()
            }

        }

    }
}