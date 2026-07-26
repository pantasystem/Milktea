package net.pantasystem.milktea.note.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_compose.haptic.rememberHapticFeedback
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.note.PollExpiresAt
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.note.editor.poll.ExpireAtType
import net.pantasystem.milktea.note.editor.poll.PollEditorLayout
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorFocusEditTextType
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel
import kotlin.time.Duration.Companion.days

/**
 * NoteEditor の最上位 Composable。
 *
 * ViewModel の状態収集とコールバックの受け渡しを担当する。
 * ダイアログの表示・ActivityResultLauncher の起動は全てラムダで上位（Fragment/Activity）に委譲する。
 *
 * Phase 3 で NoteEditorFragment.onCreateView からこの Composable をホストする。
 * Phase 4 で NoteEditorActivity が直接 setContent でこの Composable をホストする。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    accountViewModel: AccountViewModel,
    customEmojiRepository: CustomEmojiRepository,
    isPostButtonAtTheBottom: Boolean,
    onNavigateUp: () -> Unit,
    onPickFileFromDrive: () -> Unit,
    onPickFileFromLocal: () -> Unit,
    onPickImageFromLocal: () -> Unit,
    onSelectMentionUsers: () -> Unit,
    onSelectAddressUsers: () -> Unit,
    onShowEmojiPicker: () -> Unit,
    onShowDraftPicker: () -> Unit,
    onShowAlarmPermissionDescriptionDialogIfPermissionDenied: () -> Boolean,
    onShowMediaPreview: (FilePreviewSource) -> Unit,
    onEditFileCaption: (FilePreviewSource) -> Unit,
    onEditFileName: (FilePreviewSource) -> Unit,
    onShowVisibilityDialog: () -> Unit,
    onShowReservationDatePicker: () -> Unit,
    onShowReservationTimePicker: () -> Unit,
    onShowPollDatePicker: () -> Unit,
    onShowPollTimePicker: () -> Unit,
    onShowConfirmSaveAsDraftDialog: () -> Unit,
    onTextCursorPositionChanged: (Int) -> Unit = {},
    onCwCursorPositionChanged: (Int) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val replyTo by viewModel.replyTo.collectAsState()
    val addressUsers by viewModel.address.collectAsState()
    val enableFeatures by viewModel.enableFeatures.collectAsState()
    val currentAccount by viewModel.currentAccount.collectAsState()

    // フォーカスされているテキストフィールドを追跡（絵文字挿入先の判定に使う）
    var focusedField by remember { mutableStateOf(NoteEditorFocusEditTextType.Text) }
    // focusedField を ViewModel にも同期させる（EmojiPickerDialog のコールバックが ViewModel 経由のため）
    viewModel.focusType = focusedField

    // 投稿完了で画面を閉じる
    LaunchedEffect(Unit) {
        viewModel.isPost.collect { posted ->
            if (posted) onNavigateUp()
        }
    }
    // 下書き保存完了で画面を閉じる
    LaunchedEffect(Unit) {
        viewModel.isSaveNoteAsDraft.collect { draftId ->
            if (draftId != null) onNavigateUp()
        }
    }

    // 投票日時ピッカーの表示イベント
    LaunchedEffect(Unit) {
        viewModel.showPollDatePicker.collect { onShowPollDatePicker() }
    }
    LaunchedEffect(Unit) {
        viewModel.showPollTimePicker.collect { onShowPollTimePicker() }
    }

    // バックプレス：下書きが保存できる状態なら確認ダイアログを出す
    BackHandler {
        if (viewModel.canSaveDraft()) {
            onShowConfirmSaveAsDraftDialog()
        } else {
            onNavigateUp()
        }
    }

    val feedback = rememberHapticFeedback()
    val scheduleDate = uiState.sendToState.schedulePostAtAsDate

    val toolbarContent: @Composable () -> Unit = {
        NoteEditorToolbarBinding(
            noteEditorViewModel = viewModel,
            accountViewModel = accountViewModel,
            onShowAlarmPermissionDescriptionDialogIfPermissionDenied = onShowAlarmPermissionDescriptionDialogIfPermissionDenied,
            onFinishOrConfirmSaveAsDraftOrDelete = {
                if (viewModel.canSaveDraft()) onShowConfirmSaveAsDraftDialog()
                else onNavigateUp()
            },
            onShowVisibilitySelectionDialog = onShowVisibilityDialog,
        )
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (!isPostButtonAtTheBottom) {
                toolbarContent()
            }
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                NoteFilePreview(
                    noteEditorViewModel = viewModel,
                    onShow = { feedback.performClickHapticFeedback(); onShowMediaPreview(it) },
                    onEditFileCaptionSelectionClicked = {
                        feedback.performClickHapticFeedback()
                        onEditFileCaption(it)
                    },
                    onEditFileNameSelectionClicked = {
                        feedback.performClickHapticFeedback()
                        onEditFileName(it)
                    },
                )
                if (isPostButtonAtTheBottom) {
                    toolbarContent()
                }
                NoteEditorUserActionMenuLayout(
                    modifier = Modifier.fillMaxWidth(),
                    isEnableDrive = enableFeatures.contains(FeatureType.Drive),
                    iconColor = getColor(color = net.pantasystem.milktea.note.R.attr.normalIconTint),
                    isCw = uiState.formState.hasCw,
                    isPoll = uiState.poll != null,
                    onPickFileFromDriveButtonClicked = {
                        feedback.performClickHapticFeedback()
                        onPickFileFromDrive()
                    },
                    onPickFileFromLocalButtonCLicked = {
                        feedback.performClickHapticFeedback()
                        onPickFileFromLocal()
                    },
                    onPickImageFromLocalButtonClicked = {
                        feedback.performClickHapticFeedback()
                        onPickImageFromLocal()
                    },
                    onTogglePollButtonClicked = {
                        feedback.performClickHapticFeedback()
                        viewModel.enablePoll()
                    },
                    onSelectMentionUsersButtonClicked = {
                        feedback.performClickHapticFeedback()
                        onSelectMentionUsers()
                    },
                    onSelectEmojiButtonClicked = {
                        feedback.performClickHapticFeedback()
                        onShowEmojiPicker()
                    },
                    onToggleCwButtonClicked = {
                        feedback.performClickHapticFeedback()
                        viewModel.changeCwEnabled()
                    },
                    onSelectDraftNoteButtonClicked = {
                        feedback.performClickHapticFeedback()
                        onShowDraftPicker()
                    },
                )
                HorizontalDivider()
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // 入力欄・ボタン以外の空白部分をタップしたらフォーカスを外す。
                // フォーカスが外れると補完ドロップダウンが閉じる（isFocused 連動）。
                // 子の clickable / テキストフィールドはタップを消費するため影響しない。
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .verticalScroll(rememberScrollState()),
        ) {
            // リプライ先プレビュー
            replyTo?.let { reply ->
                NoteEditorReplyPreview(replyTo = reply)
            }

            // 予約投稿セクション
            if (scheduleDate != null) {
                NoteEditorScheduleSection(
                    scheduleDate = scheduleDate,
                    onPickDateClicked = onShowReservationDatePicker,
                    onPickTimeClicked = onShowReservationTimePicker,
                    onClearClicked = { viewModel.toggleReservationAt() },
                )
            }

            // 宛先ユーザーセクション（Specified visibility のみ表示）
            if (uiState.sendToState.visibility is Visibility.Specified) {
                NoteEditorAddressSection(
                    addressUsers = addressUsers,
                    onAddAddressClicked = {
                        feedback.performClickHapticFeedback()
                        onSelectAddressUsers()
                    },
                )
            }

            // テキスト入力エリア
            NoteEditorTextInputSection(
                text = uiState.formState.text ?: "",
                cw = uiState.formState.cw,
                hasCw = uiState.formState.hasCw,
                account = currentAccount,
                customEmojiRepository = customEmojiRepository,
                textCursorPosFlow = viewModel.textCursorPos,
                onTextChanged = { viewModel.setText(it) },
                onCwChanged = { viewModel.setCw(it) },
                onFocusChanged = { focusedField = it },
                onUrlPasted = { text, start, beforeText, count ->
                    viewModel.onPastePostUrl(text, start, beforeText, count)
                },
                onTextCursorPositionChanged = onTextCursorPositionChanged,
                onCwCursorPositionChanged = onCwCursorPositionChanged,
            )

            // 投票エディタ
            val pollState = uiState.poll
            if (pollState != null) {
                PollEditorLayout(
                    modifier = Modifier.padding(8.dp),
                    uiState = pollState,
                    onInput = { id, value -> viewModel.changePollChoice(id, value) },
                    onAddAnswerButtonClicked = { viewModel.addPollChoice() },
                    onRemove = { viewModel.removePollChoice(it) },
                    onExpireAtTypeChanged = { type ->
                        when (type) {
                            ExpireAtType.IndefinitePeriod ->
                                viewModel.setPollExpiresAt(PollExpiresAt.Infinity)
                            ExpireAtType.SpecificDateAndTime ->
                                viewModel.setPollExpiresAt(
                                    PollExpiresAt.DateAndTime(Clock.System.now().plus(1.days))
                                )
                        }
                    },
                    onExpireAtChangeDateButtonClicked = viewModel::onExpireAtChangeDateButtonClicked,
                    onExpireAtChangeTimeButtonClicked = viewModel::onExpireAtChangeTimeButtonClicked,
                    onMultipleAnswerTypeChanged = { viewModel.togglePollMultiple() },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
