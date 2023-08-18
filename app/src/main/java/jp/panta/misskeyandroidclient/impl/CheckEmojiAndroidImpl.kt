package jp.panta.misskeyandroidclient.impl

import net.pantasystem.milktea.model.note.reaction.CheckEmoji
import javax.inject.Inject

class CheckEmojiAndroidImpl @Inject constructor(
) : CheckEmoji {
    override suspend fun checkEmoji(char: CharSequence): Boolean {
//        return (EmojiCompat.get()?.hasEmojiGlyph(char) ?: false) || utf8EmojiRepository.exists(char)
        // TODO: 正しく判定できるように修正する
        return true
    }
}