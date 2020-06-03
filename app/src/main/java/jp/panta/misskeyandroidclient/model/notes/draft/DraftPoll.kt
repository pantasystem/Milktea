package jp.panta.misskeyandroidclient.model.notes.draft

import java.io.Serializable

data class DraftPoll(
    var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
): Serializable