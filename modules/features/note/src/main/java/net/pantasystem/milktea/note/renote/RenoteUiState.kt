package net.pantasystem.milktea.note.renote

import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User

data class RenoteViewModelUiState(
    val targetNoteId: Note.Id? = null,
    val noteState: RenoteViewModelTargetNoteState = RenoteViewModelTargetNoteState(
        ResultState.initialState(),
        null
    ),
    val accounts: List<AccountInfo> = emptyList(),
    val canQuote: Boolean = true,
    val isRenoteButtonVisible: Boolean = true,
    val isChannelRenoteButtonVisible: Boolean = false,
)

data class RenoteViewModelTargetNoteState(
    val syncState: ResultState<Unit>,
    val note: Note?,
)

data class AccountInfo(
    val accountId: Long,
    val user: User,
    val isSelected: Boolean,
    val isEnable: Boolean,
    val instanceIconUrl: String?,
)