package jp.panta.misskeyandroidclient.impl

import androidx.emoji.text.EmojiCompat
import net.pantasystem.milktea.model.emoji.UtfEmojiRepository
import net.pantasystem.milktea.model.notes.reaction.CheckEmoji
import javax.inject.Inject

class CheckEmojiAndroidImpl @Inject constructor(
    private val utf8EmojiRepository: UtfEmojiRepository
) : CheckEmoji {
    override suspend fun checkEmoji(char: CharSequence): Boolean {
        return (EmojiCompat.get()?.hasEmojiGlyph(char) ?: false) || utf8EmojiRepository.exists(char)
    }
}