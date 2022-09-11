package net.pantasystem.milktea.note.emojis.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.emoji.Emoji
import javax.inject.Inject


@HiltViewModel
class EmojiSelectionViewModel @Inject constructor(): ViewModel(), EmojiSelection {

    val selectedEmoji = EventBus<Emoji>()
    val selectedEmojiName = EventBus<String>()
    override fun onSelect(emoji: Emoji) {
        selectedEmoji.event = emoji
    }


    override fun onSelect(emoji: String) {
        selectedEmojiName.event = emoji
    }

}