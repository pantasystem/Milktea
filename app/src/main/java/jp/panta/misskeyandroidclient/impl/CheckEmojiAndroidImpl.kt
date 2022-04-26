package jp.panta.misskeyandroidclient.impl

import androidx.emoji.text.EmojiCompat
import net.pantasystem.milktea.model.notes.reaction.CheckEmoji

class CheckEmojiAndroidImpl : CheckEmoji {
    override fun checkEmoji(char: CharSequence): Boolean {
        return EmojiCompat.get()?.hasEmojiGlyph(char) ?: false
    }
}