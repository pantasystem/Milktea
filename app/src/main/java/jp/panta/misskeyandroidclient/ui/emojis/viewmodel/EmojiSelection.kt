package jp.panta.misskeyandroidclient.ui.emojis.viewmodel

import jp.panta.misskeyandroidclient.model.emoji.Emoji

interface EmojiSelection{

    fun onSelect(emoji: Emoji)
    fun onSelect(emoji: String)
}