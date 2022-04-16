package jp.panta.misskeyandroidclient.ui.emojis.viewmodel

import androidx.lifecycle.ViewModel
import net.pantasystem.milktea.data.model.emoji.Emoji
import jp.panta.misskeyandroidclient.util.eventbus.EventBus


class EmojiSelectionViewModel : ViewModel(), EmojiSelection {

    val selectedEmoji = EventBus<Emoji>()
    val selectedEmojiName = EventBus<String>()
    override fun onSelect(emoji: Emoji) {
        selectedEmoji.event = emoji
    }


    override fun onSelect(emoji: String) {
        selectedEmojiName.event = emoji
    }

}