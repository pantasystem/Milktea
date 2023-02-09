package net.pantasystem.milktea.note.timeline

import android.content.Context
import android.util.Log
import android.widget.Toast
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.note.R
import java.io.IOException
import java.net.SocketTimeoutException

class TimelineErrorHandler(
    val context: Context,
) {

    operator fun invoke(error: Throwable) {
        Log.e("TimelineErrorHandler", "error", error)
        when (error) {
            is SocketTimeoutException -> {
                Toast.makeText(context, R.string.timeout_error, Toast.LENGTH_LONG)
                    .show()
            }
            is IOException -> {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG)
                    .show()

            }
            is APIError.AuthenticationException -> {
                Toast.makeText(context, R.string.auth_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.ForbiddenException -> {
                Toast.makeText(context, R.string.auth_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.IAmAIException -> {
                Toast.makeText(context, R.string.bot_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.InternalServerException -> {
                Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.ClientException -> {
                Toast.makeText(
                    context,
                    R.string.parameter_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            is APIError.ToManyRequestsException -> {
                Toast.makeText(
                    context,
                    R.string.timeline_rate_limit_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            is UnauthorizedException -> {
                Toast.makeText(
                    context,
                    R.string.timeline_unauthorized_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(context, "error:$error", Toast.LENGTH_SHORT).show()
            }

        }

    }
}