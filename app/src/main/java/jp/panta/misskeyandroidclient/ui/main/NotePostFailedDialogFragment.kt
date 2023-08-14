package jp.panta.misskeyandroidclient.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.worker.note.CreateNoteWorker
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import javax.inject.Inject

@AndroidEntryPoint
class NotePostFailedDialogFragment : AppCompatDialogFragment() {

    companion object {
        private const val DRAFT_NOTE_ID = "DRAFT_NOTE_ID"
        private const val ERROR_REASON_TYPE = "ERROR_REASON_TYPE"
        private const val STACKTRACE = "STACKTRACE"

        fun newInstance(
            draftNoteId: Long,
            errorReasonType: CreateNoteWorker.ErrorReasonType,
            stackTrace: String?,
        ): NotePostFailedDialogFragment {
            return NotePostFailedDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(DRAFT_NOTE_ID, draftNoteId)
                    putString(ERROR_REASON_TYPE, errorReasonType.name)
                    putString(STACKTRACE, stackTrace)
                }
            }
        }
    }


    @Inject
    internal lateinit var createNoteWorkerExecutor: CreateNoteWorkerExecutor

    private val draftNoteId: Long by lazy {
        requireArguments().getLong(DRAFT_NOTE_ID)
    }

    private val errorReasonType: CreateNoteWorker.ErrorReasonType by lazy {
        requireArguments().getString(ERROR_REASON_TYPE)?.let { type ->
            CreateNoteWorker.ErrorReasonType.values().find {
                it.name == type
            }
        } ?: CreateNoteWorker.ErrorReasonType.UnknownError
    }

    private val stackTrace: String? by lazy {
        requireArguments().getString(STACKTRACE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.note_creation_failure)
            .setPositiveButton(R.string.retry){ _, _ ->
                createNoteWorkerExecutor.enqueue(draftNoteId)
            }
            .setView(
                ComposeView(requireContext()).apply {
                    setContent {
                        MdcTheme {
                            var isVisibleDetailMessage by remember { mutableStateOf(false) }
                            Column(Modifier.padding(horizontal = 16.dp)) {
                                Text(
                                    getReasonText(
                                        errorReasonType = errorReasonType,
                                        stackTrace = stackTrace
                                    ),
                                    Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                )
                                TextButton(onClick = { isVisibleDetailMessage = !isVisibleDetailMessage }) {
                                    Text(stringResource(id = R.string.show_error))
                                }
                                AnimatedVisibility(
                                    visible = isVisibleDetailMessage,
                                ) {
                                    TextField(
                                        stackTrace ?: "Unknown Error",
                                        onValueChange = {},
                                        modifier = Modifier.heightIn(
                                            min = 100.dp,
                                            max = 300.dp
                                        ).verticalScroll(rememberScrollState()),
                                    )
                                }
                            }
                        }
                    }
                }
            )
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }
}

@Composable
fun getReasonText(errorReasonType: CreateNoteWorker.ErrorReasonType, stackTrace: String?): String {
    val context = LocalContext.current
    return when(errorReasonType) {
        CreateNoteWorker.ErrorReasonType.NetworkError -> context.getString(R.string.network_error)
        CreateNoteWorker.ErrorReasonType.FileUploadDeviceSecurityError -> "Device File Security Error"
        CreateNoteWorker.ErrorReasonType.FileUploadDriveNoFreeSpaceError -> context.getString(R.string.misskey_role_drive_no_free_space_error_message)
        CreateNoteWorker.ErrorReasonType.ServerError -> context.getString(R.string.server_error)
        CreateNoteWorker.ErrorReasonType.ClientError -> context.getString(R.string.parameter_error)
        CreateNoteWorker.ErrorReasonType.UnauthorizedError -> context.getString(R.string.unauthorized_error)
        CreateNoteWorker.ErrorReasonType.IAmAiError -> context.getString(R.string.bot_error)
        CreateNoteWorker.ErrorReasonType.ToManyRequestError -> context.getString(R.string.rate_limit_error)
        CreateNoteWorker.ErrorReasonType.NotFoundError -> context.getString(R.string.not_found_error)
        CreateNoteWorker.ErrorReasonType.UnknownError -> stackTrace ?: "Unknown Error"
    }
}