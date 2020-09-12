package jp.panta.misskeyandroidclient.model.account.db

import androidx.room.Entity
import jp.panta.misskeyandroidclient.model.emoji.Emoji

@Entity(tableName = "account_emoji_table",
primaryKeys = ["name", "accountId"])
data class AccountEmoji(
    val name: String,
    val accountId: Long,
    val url: String
) {
    fun toEmoji(): Emoji{
        return Emoji(
            name = name,
            url = url,
            host = null,
            uri = null,
            type = null,
            category = null,
            id = null
        )
    }
}


fun Emoji.toAccountEmoji(accountId: Long): AccountEmoji{
    return AccountEmoji(
        name = this.name,
        url = this.url?: this.uri!!,
        accountId = accountId
    )
}