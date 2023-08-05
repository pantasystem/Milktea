package net.pantasystem.milktea.note.editor.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility

class NoteEditorVisibilityCombiner(
    private val scope: CoroutineScope,
    private val localConfigRepository: LocalConfigRepository,
) {

    fun create(
        visibilityFlow: StateFlow<Visibility?>,
        currentAccountFlow: StateFlow<Account?>,
        channelIdFlow: StateFlow<Channel.Id?>,
    ): StateFlow<Visibility> {
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
        }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), Visibility.Public(false))
    }
}