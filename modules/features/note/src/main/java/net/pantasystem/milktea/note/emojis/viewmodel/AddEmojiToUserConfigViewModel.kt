package net.pantasystem.milktea.note.emojis.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.emoji.AddEmojiToUserConfigUseCase
import javax.inject.Inject

@HiltViewModel
class AddEmojiToUserConfigViewModel @Inject constructor(
//    private val customEmojiRepository: CustomEmojiRepository,
    val addEmojiToUserConfigUseCase: AddEmojiToUserConfigUseCase,
    private val savedStateHandle: SavedStateHandle,
//    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
): ViewModel() {
    companion object {
        const val EXTRA_TEXT_EMOJI = "AddEmojiToDeckDialog.EXTRA_TEXT_EMOJI"
    }

    private val logger by lazy {
        loggerFactory.create("AddEmojiToDeckVM")
    }
//
//    private val textEmojiType = savedStateHandle.getStateFlow<String?>(EXTRA_TEXT_EMOJI, null)
//    @OptIn(ExperimentalCoroutinesApi::class)
//    private val currentAccountHostsEmojis = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
//        customEmojiRepository.observeBy(it.getHost())
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
//
//    val currentEmojiType = combine(textEmojiType, currentAccountHostsEmojis) { t, emojis ->
//        t?.let {
//            EmojiType.from(emojis, it)
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(textEmoji: String) {
        viewModelScope.launch {
            addEmojiToUserConfigUseCase(textEmoji).onFailure {
                logger.error("絵文字のユーザ設定への追加に失敗", it)
            }
        }
    }

}