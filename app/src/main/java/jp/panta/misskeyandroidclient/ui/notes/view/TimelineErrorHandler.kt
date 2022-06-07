package jp.panta.misskeyandroidclient.ui.notes.view

import android.content.Context
import android.widget.Toast
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.common.APIError
import java.io.IOException
import java.net.SocketTimeoutException

class TimelineErrorHandler(
    val context: Context,
) {

    operator fun invoke(error: Throwable) {
        when (error) {
            is IOException -> {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG)
                    .show()

            }
            is SocketTimeoutException -> {
                Toast.makeText(context, R.string.timeout_error, Toast.LENGTH_LONG)
                    .show()

            }
            is APIError.AuthenticationException -> {
                Toast.makeText(context, R.string.auth_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.IAmAIException -> {
                Toast.makeText(context, R.string.bot_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.InternalServerException -> {
                Toast.makeText(context, R.string.auth_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError.ClientException -> {
                Toast.makeText(
                    context,
                    R.string.parameter_error,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(context, "error:$error", Toast.LENGTH_SHORT).show()
            }

        }

    }
}