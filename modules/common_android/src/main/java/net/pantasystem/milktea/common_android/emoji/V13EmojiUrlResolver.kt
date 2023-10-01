package net.pantasystem.milktea.common_android.emoji

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.reaction.Reaction

/**
 * V13でEmojiのURLの取得方法が変更されたので、特定のソースからURLを解決するためのクラス
 */
object V13EmojiUrlResolver {

    /**
     * MFMParserからの使用を想定
     */
    fun resolve(
        accountHost: String?,
        tagName: String,
        emojiHost: String?,
    ): String {
        if (emojiHost == null || emojiHost == accountHost) {
            return "https://$accountHost/emoji/${tagName}.webp"
        }
        return "https://$accountHost/emoji/${tagName}@${emojiHost}.webp"
    }

    /**
     * localで使用されているEmojiのURLの解決を想定している
     */
    fun resolve(
        emoji: CustomEmoji,
        instanceDomain: String
    ): String {
        return "$instanceDomain/emoji/${emoji.name}.webp"
    }

    /**
     * リアクションのEmojiのURLを解決することを想定している
     */
    fun resolve(
        reaction: Reaction,
        account: Account
    ): String {
        return "https://${account.getHost()}/emoji/${reaction.reaction.replace(":", "")}.webp"
    }
}