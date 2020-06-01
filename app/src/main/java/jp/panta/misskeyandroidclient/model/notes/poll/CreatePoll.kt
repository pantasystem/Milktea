package jp.panta.misskeyandroidclient.model.notes.poll

import java.io.Serializable

data class CreatePoll(
    var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
): Serializable