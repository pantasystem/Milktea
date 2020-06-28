package jp.panta.misskeyandroidclient.viewmodel.emojis

import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.util.eventbus.EventBus


class EmojiSelectionViewModel : ViewModel(), EmojiSelection{

    val selectedEmoji = EventBus<Emoji>()
    val selectedEmojiName = EventBus<String>()
    override fun onSelect(emoji: Emoji) {
        selectedEmoji.event = emoji
    }


    override fun onSelect(emoji: String) {
        selectedEmojiName.event = emoji
    }

}