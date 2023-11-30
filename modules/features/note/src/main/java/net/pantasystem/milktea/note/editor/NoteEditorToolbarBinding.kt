package net.pantasystem.milktea.note.editor

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_compose.haptic.rememberHapticFeedback
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@Composable
fun NoteEditorToolbarBinding(
    noteEditorViewModel: NoteEditorViewModel,
    accountViewModel: AccountViewModel,
    onShowAlarmPermissionDescriptionDialogIfPermissionDenied: () -> Boolean,
    onFinishOrConfirmSaveAsDraftOrDelete: () -> Unit,
    onShowVisibilitySelectionDialog: () -> Unit,
) {
    val currentUser by noteEditorViewModel.user.collectAsState()
    val uiState by noteEditorViewModel.uiState.collectAsState()
    val isPostAvailable by noteEditorViewModel.isPostAvailable.collectAsState()
    val feedback = rememberHapticFeedback()
    NoteEditorToolbar(
        currentUser = currentUser,
        visibility = uiState.sendToState.visibility,
        validInputs = isPostAvailable,
        textCount = uiState.formState.text?.let {
            it.codePointCount(0, it.length)
        } ?: 0,
        onNavigateUpButtonClicked = {
            feedback.performClickHapticFeedback()
            onFinishOrConfirmSaveAsDraftOrDelete()
        },
        onAvatarIconClicked = {
            feedback.performClickHapticFeedback()
            accountViewModel.showSwitchDialog()
        },
        onVisibilityButtonClicked = {
            feedback.performClickHapticFeedback()
            onShowVisibilitySelectionDialog()
        },
        onScheduleButtonClicked = {
            feedback.performClickHapticFeedback()
            if (uiState.sendToState.schedulePostAt == null) {
                // check alarm permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (onShowAlarmPermissionDescriptionDialogIfPermissionDenied()) {
                        return@NoteEditorToolbar
                    }
                }
            }
            noteEditorViewModel.toggleReservationAt()
        },
        onPostButtonClicked = {
            feedback.performClickHapticFeedback()
            noteEditorViewModel.post()
        },
    )
}