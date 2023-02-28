package net.pantasystem.milktea.note.emojis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.emoji.AddEmojiToUserConfigUseCase
import javax.inject.Inject

@HiltViewModel
class AddEmojiToUserConfigViewModel @Inject constructor(
    val addEmojiToUserConfigUseCase: AddEmojiToUserConfigUseCase,
    loggerFactory: Logger.Factory,
): ViewModel() {
    companion object {
        const val EXTRA_TEXT_EMOJI = "AddEmojiToDeckDialog.EXTRA_TEXT_EMOJI"
    }

    private val logger by lazy {
        loggerFactory.create("AddEmojiToDeckVM")
    }

    fun save(textEmoji: String) {
        viewModelScope.launch {
            addEmojiToUserConfigUseCase(textEmoji).onFailure {
                logger.error("絵文字のユーザ設定への追加に失敗", it)
            }
        }
    }

}