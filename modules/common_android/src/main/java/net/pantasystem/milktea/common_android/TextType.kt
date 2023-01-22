package net.pantasystem.milktea.common_android

import androidx.core.text.HtmlCompat
import jp.panta.misskeyandroidclient.mfm.Root
import net.pantasystem.milktea.common_android.html.MastodonHTML
import net.pantasystem.milktea.common_android.html.MastodonHTMLParser
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.NoteRelation

sealed interface TextType {
    data class Misskey(val root: Root?) : TextType
    data class Mastodon(val html: MastodonHTML) : TextType
}

fun getTextType(account: Account, note: NoteRelation, instanceEmojis: List<Emoji>): TextType? {
    return when (account.instanceType) {
        Account.InstanceType.MISSKEY -> {
            note.note.text?.let {
                TextType.Misskey(
                    MFMParser.parse(
                        note.note.text, (note.note.emojis ?: emptyList()) + instanceEmojis,
                        userHost = note.user
                            .host,
                        accountHost = account.getHost()
                    )
                )
            }
        }
        Account.InstanceType.MASTODON -> {
            note.note.text?.let {
                TextType.Mastodon(
                    MastodonHTMLParser.parse(
                        it, note.note.emojis ?: emptyList(), userHost = note.user
                            .host,
                        accountHost = account.getHost()
                    )
                )
            }
        }
    }

}