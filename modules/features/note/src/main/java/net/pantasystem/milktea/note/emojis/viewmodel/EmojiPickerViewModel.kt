package net.pantasystem.milktea.note.emojis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.UserEmojiConfigRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import net.pantasystem.milktea.note.EmojiPickerUiStateService
import javax.inject.Inject

@HiltViewModel
class EmojiPickerViewModel @Inject constructor(
    accountStore: AccountStore,
    reactionHistoryDao: ReactionHistoryRepository,
    userEmojiConfigRepository: UserEmojiConfigRepository,
    customEmojiRepository: CustomEmojiRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {
    private val logger = loggerFactory.create("EmojiPickerViewModel")

    private val uiStateService = EmojiPickerUiStateService(
        accountStore = accountStore,
        reactionHistoryRepository = reactionHistoryDao,
        userEmojiConfigRepository = userEmojiConfigRepository,
        coroutineScope = viewModelScope,
        customEmojiRepository = customEmojiRepository,
        logger = logger,
    )

    val searchWord = uiStateService.searchWord

    // 検索時の候補
    val uiState = uiStateService.uiState

    val tabLabels = uiStateService.tabLabels

}
