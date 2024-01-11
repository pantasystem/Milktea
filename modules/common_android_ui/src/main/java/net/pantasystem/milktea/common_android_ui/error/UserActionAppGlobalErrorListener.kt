package net.pantasystem.milktea.common_android_ui.error

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.app_store.handler.AppGlobalError
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorStore
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common_android.resource.getString
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.model.account.UnauthorizedException
import java.io.IOException
import javax.inject.Inject

class UserActionAppGlobalErrorListener @Inject constructor(
    private val userActionAppGlobalErrorStore: UserActionAppGlobalErrorStore,
    @ApplicationContext private val context: Context,
) {

    operator fun invoke(
        lifecycle: Lifecycle,
        fragmentManager: FragmentManager,
    ) {
        userActionAppGlobalErrorStore.errorFlow.onEach { appGlobalError ->
            when (appGlobalError.level) {
                AppGlobalError.ErrorLevel.Info -> {}
                AppGlobalError.ErrorLevel.Warning -> {
                    // toastで表示する
                    Toast.makeText(
                        context,
                        appGlobalError.message.getString(context),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

                AppGlobalError.ErrorLevel.Error -> {
                    val body = when (val error = appGlobalError.throwable) {
                        is IOException -> {
                            context.getString(R.string.network_error)
                        }

                        is APIError -> {
                            APIErrorStringConverter()(error).getString(context)
                        }

                        is UnauthorizedException -> {
                            context.getString(R.string.unauthorized_error)
                        }

                        else -> "Unknown error"

                    }
                    val dialogInstance = UserActionAppGlobalErrorDialog.newInstance(
                        title = appGlobalError.message.getString(context),
                        message = body,
                        detail = appGlobalError.throwable?.toString(),
                    )
                    fragmentManager.findFragmentByTag("error_dialog")?.let {
                        fragmentManager.beginTransaction().remove(it).commit()
                    }
                    dialogInstance.show(fragmentManager, "error_dialog")
                }
            }
        }.flowWithLifecycle(
            lifecycle,
            Lifecycle.State.RESUMED,
        ).launchIn(lifecycle.coroutineScope)
    }
}

class UserActionAppGlobalErrorDialog : DialogFragment() {
    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_DETAIL = "detail"

        fun newInstance(title: String?, message: String, detail: String?): UserActionAppGlobalErrorDialog {
            return UserActionAppGlobalErrorDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TITLE, title)
                    putString(EXTRA_MESSAGE, message)
                    // 1MB未満にする
                    putString(EXTRA_DETAIL, detail?.take(1024 * 1024 - 1))
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .also {  dialog ->
                if (arguments?.getString(EXTRA_TITLE) != null) {
                    dialog.setTitle(
                        arguments?.getString(EXTRA_TITLE) ?: ""
                    )
                }
                if (arguments?.getString(EXTRA_DETAIL) != null) {
                    dialog.setNeutralButton(R.string.detail) { _, _ ->
                        val detail = arguments?.getString(EXTRA_DETAIL) ?: ""
                        if (detail.isNotEmpty()) {
                            val d = newInstance(
                                title = null,
                                message = detail,
                                detail = null,
                            )
                            d.show(parentFragmentManager, "error_dialog")
                        }
                    }
                }
            }
            .setMessage(
                arguments?.getString(EXTRA_MESSAGE) ?: ""
            )
            .setPositiveButton(android.R.string.ok) { _, _ ->
                dismiss()
            }
            .create()
    }
}