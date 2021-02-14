package jp.panta.misskeyandroidclient.model.notes.poll

import jp.panta.misskeyandroidclient.serializations.DateSerializer
import java.io.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class Poll(
    val choices: List<Choice>,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val expiresAt: Date?,
    val multiple: Boolean
): Serializable{

    @kotlinx.serialization.Serializable
    data class Choice(val text: String, val votes: Int, val isVoted: Boolean): Serializable
}