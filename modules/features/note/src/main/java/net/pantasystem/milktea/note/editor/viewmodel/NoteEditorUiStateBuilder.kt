package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.PollEditingState
import net.pantasystem.milktea.model.note.ReactionAcceptanceType
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility
import java.util.Date
import javax.inject.Inject

class NoteEditorUiStateBuilder @Inject constructor(
) {

    operator fun invoke(
        textFlow: Flow<String?>,
        cwFlow: Flow<String?>,
        hasCwFlow: Flow<Boolean>,
        isSensitiveMediaFlow: Flow<Boolean?>,
        filePreviewSourcesFlow: Flow<List<FilePreviewSource>>,
        pollFlow: Flow<PollEditingState?>,
        currentAccountFlow: Flow<Account?>,
        noteEditorSendToStateFlow: Flow<NoteEditorSendToState>,
    ): Flow<NoteEditorUiState> {


        val noteEditorFormState = combine(textFlow, cwFlow, hasCwFlow, isSensitiveMediaFlow) { text, cw, hasCw, sensitive ->
                NoteEditorFormState(
                    text = text,
                    cw = cw,
                    hasCw = hasCw,
                    isSensitive = sensitive ?: false,
                )
            }


        return combine(
            noteEditorFormState,
            noteEditorSendToStateFlow,
            filePreviewSourcesFlow,
            pollFlow,
            currentAccountFlow,
        ) { formState, sendToState, files, poll, account ->


            NoteEditorUiState(
                formState = formState,
                sendToState = sendToState,
                poll = poll,
                files = files,
                currentAccount = account,
            )
        }
    }
}

class NoteEditorSendToStateBuilder @Inject constructor(
    private val localConfigRepository: LocalConfigRepository,
) {

    operator fun invoke(
        visibilityFlow: StateFlow<Visibility?>,
        currentAccountFlow: StateFlow<Account?>,
        channelIdFlow: StateFlow<Channel.Id?>,
        replyIdFlow: Flow<Note.Id?>,
        renoteIdFlow: Flow<Note.Id?>,
        reservationPostingAtFlow: Flow<Date?>,
        draftNoteIdFlow: Flow<Long?>,
        reactionAcceptanceType: Flow<ReactionAcceptanceType?>,
    ): Flow<NoteEditorSendToState> {
        val visibility = create(visibilityFlow, currentAccountFlow, channelIdFlow)
        val visibilityAndChannelId = combine(visibility, channelIdFlow) { v, c ->
            VisibilityAndChannelId(v, c)
        }
        val replyIdAndRenoteId = combine(replyIdFlow, renoteIdFlow) { replyId, renoteId ->
            replyId to renoteId
        }
        return combine(
            visibilityAndChannelId,
            replyIdAndRenoteId,
            reservationPostingAtFlow,
            draftNoteIdFlow,
            reactionAcceptanceType
        ) { vc, (replyId, renoteId), scheduleDate, dfId, reactionAcceptance ->
            NoteEditorSendToState(
                visibility = vc.visibility,
                channelId = vc.channelId,
                replyId = replyId,
                renoteId = renoteId,
                schedulePostAt = scheduleDate?.let {
                    Instant.fromEpochMilliseconds(it.time)
                },
                draftNoteId = dfId,
                reactionAcceptanceType = reactionAcceptance,
            )
        }
    }

    private fun create(
        visibilityFlow: StateFlow<Visibility?>,
        currentAccountFlow: StateFlow<Account?>,
        channelIdFlow: StateFlow<Channel.Id?>,
    ): Flow<Visibility> {
        return combine(visibilityFlow, currentAccountFlow.filterNotNull().map {
            localConfigRepository.getRememberVisibility(it.accountId).getOrElse {
                RememberVisibility.None
            }
        }, channelIdFlow) { formVisibilityState, settingVisibilityState, channelId ->
            when {
                formVisibilityState != null -> formVisibilityState
                settingVisibilityState is RememberVisibility.None -> Visibility.Public(false)
                settingVisibilityState is RememberVisibility.Remember -> settingVisibilityState.visibility
                channelId != null -> Visibility.Public(true)
                else -> Visibility.Public(false)
            }
        }
    }
}