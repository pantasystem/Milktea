package net.pantasystem.milktea.note.renote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.coroutines.combine
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRelation
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.isLocalOnly
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class RenoteUiStateBuilder @Inject constructor(
    private val userRepository: UserRepository,
    private val instanceInfoService: InstanceInfoService,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun buildState(
        targetNoteIdFlow: StateFlow<Note.Id?>,
        noteFlow: Flow<NoteRelation?>,
        channelFlow: Flow<Channel?>,
        noteSyncState: Flow<ResultState<Unit>>,
        selectedAccountIds: Flow<List<Long>>,
        accountsFlow: Flow<List<Account>>,
        currentAccountInstanceInfo: Flow<InstanceInfoType?>,
        coroutineScope: CoroutineScope,
    ): Flow<RenoteViewModelUiState> {
        val accountAndUserList = accountsFlow.map { accounts ->
            accounts.map {
                it to User.Id(
                    it.accountId,
                    it.remoteId
                )
            }.map { (account, userId) ->
                userRepository.observe(userId).map { user ->
                    account to user
                }
            }
        }.flatMapLatest { flows ->
            combine(flows) { users ->
                users.toList()
            }
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

        val noteState = combine(noteFlow, noteSyncState) { n, s ->
            RenoteViewModelTargetNoteState(
                note = n?.contentNote?.note,
                syncState = s
            )
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            RenoteViewModelTargetNoteState(
                syncState = ResultState.initialState(),
                note = null
            )
        )

        val instanceInfoListFlow = accountsFlow.flatMapLatest { accounts ->
            instanceInfoService.observeIn(
                accounts.map {
                    it.normalizedInstanceUri
                }
            )
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

        val accountWithUsers = combine(
            accountAndUserList,
            selectedAccountIds,
            noteFlow,
            instanceInfoListFlow,
        ) { accountWithUser, selectedIds, note, instanceInfoList ->
            accountWithUser.map { (account, user) ->
                AccountInfo(
                    accountId = account.accountId,
                    user = user,
                    isSelected = selectedIds.any { id -> id == account.accountId },
                    isEnable = note?.contentNote?.canRenote(account, user) == true,
                    instanceIconUrl = instanceInfoList.firstOrNull {
                        it.uri == account.normalizedInstanceUri
                    }?.iconUrl
                )
            }
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

        val isRenoteButtonVisibleFlow = channelFlow.map { it?.allowRenoteToExternal ?: true }
        val isChannelRenoteButtonVisibleFlow = channelFlow.map { it != null }

        return combine(
            targetNoteIdFlow,
            noteState,
            accountWithUsers,
            currentAccountInstanceInfo,
            isRenoteButtonVisibleFlow,
            isChannelRenoteButtonVisibleFlow,
        ) { noteId, syncState, accounts, instanceInfo, isRenoteButtonVisible, isChannelRenoteButtonVisible ->
            RenoteViewModelUiState(
                targetNoteId = noteId,
                noteState = syncState,
                accounts = accounts,
                canQuote = instanceInfo?.canQuote ?: false,
                isRenoteButtonVisible = isRenoteButtonVisible,
                isChannelRenoteButtonVisible = isChannelRenoteButtonVisible,
            )
        }
    }
}

fun NoteRelation.canRenote(account: Account, user: User): Boolean {
    if (note.canRenote(user.id)) {
        return true
    }

    // NOTE: 公開範囲がpublicなら可能
    if (note.visibility is Visibility.Public && !note.visibility.isLocalOnly()) {
        return true
    }

    // NOTE: 公開範囲がフォロワー、DMの場合は無理
    if (note.visibility is Visibility.Followers || note.visibility is Visibility.Specified) {
        return false
    }

    // NOTE: 同一ホストなら可能
    if (note.visibility.isLocalOnly() && this.user.host == account.getHost()) {
        return true
    }

    // NOTE: 投稿のホストとアカウントが同一ホストかつ公開範囲の場合はRenote可能
    if (note.visibility is Visibility.Home && this.user.host == account.getHost()) {
        return true
    }

    return false
}