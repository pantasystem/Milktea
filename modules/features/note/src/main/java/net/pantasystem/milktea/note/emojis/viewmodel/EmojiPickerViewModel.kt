package net.pantasystem.milktea.note.emojis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import net.pantasystem.milktea.note.EmojiPickerUiStateService
import javax.inject.Inject

@HiltViewModel
class EmojiPickerViewModel @Inject constructor(
    accountStore: AccountStore,
    metaRepository: MetaRepository,
    reactionHistoryDao: ReactionHistoryRepository,
    userEmojiConfigRepository: UserEmojiConfigRepository,
) : ViewModel() {

    private val uiStateService = EmojiPickerUiStateService(
        accountStore = accountStore,
        metaRepository = metaRepository,
        reactionHistoryRepository = reactionHistoryDao,
        userEmojiConfigRepository = userEmojiConfigRepository,
        coroutineScope = viewModelScope,
    )

    val searchWord = uiStateService.searchWord

    // 検索時の候補
    val uiState = uiStateService.uiState

    val tabLabels = uiStateService.tabLabels

}
