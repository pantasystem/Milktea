package jp.panta.misskeyandroidclient.model.notes.poll

import java.io.Serializable
import java.util.*

data class Poll(
    val choices: List<Choice>,
    val expiresAt: Date?,
    val multiple: Boolean
): Serializable{
    data class Choice(val text: String, val votes: Int, val isVoted: Boolean): Serializable
}