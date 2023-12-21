package net.pantasystem.milktea.user.profile

import android.content.Context
import android.widget.Toast
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common_android.resource.getString
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.note.R
import java.io.IOException

class UserDetailErrorHandler(val context: Context) {
    operator fun invoke(throwable: Throwable) {
        when (throwable) {
            is IOException -> {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG)
                    .show()
            }
            is APIError -> {
                Toast.makeText(context, APIErrorStringConverter()(throwable).getString(context), Toast.LENGTH_LONG)
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

            }

        }
    }
}