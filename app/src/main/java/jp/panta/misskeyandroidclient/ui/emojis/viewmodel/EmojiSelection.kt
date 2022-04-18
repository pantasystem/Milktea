package jp.panta.misskeyandroidclient.ui.emojis.viewmodel

import net.pantasystem.milktea.model.emoji.Emoji

interface EmojiSelection{

    fun onSelect(emoji: net.pantasystem.milktea.model.emoji.Emoji)
    fun onSelect(emoji: String)
}