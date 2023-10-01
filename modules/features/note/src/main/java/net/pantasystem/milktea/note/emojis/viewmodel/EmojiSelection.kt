package net.pantasystem.milktea.note.emojis.viewmodel

import net.pantasystem.milktea.model.emoji.CustomEmoji

interface EmojiSelection{

    fun onSelect(emoji: CustomEmoji)
    fun onSelect(emoji: String)
}