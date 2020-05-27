package jp.panta.misskeyandroidclient.viewmodel.emojis

import jp.panta.misskeyandroidclient.model.emoji.Emoji

interface EmojiSelection{

    fun onSelect(emoji: Emoji)
    fun onSelect(emoji: String)
}