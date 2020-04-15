package jp.panta.misskeyandroidclient.model.notes.poll

import java.io.Serializable

data class Poll(
    val choices: List<Choice>,
    val expiresAt: String,
    val multiple: Boolean
): Serializable{
    data class Choice(val text: String, val votes: Int, val isVoted: Boolean): Serializable
}