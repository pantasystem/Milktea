package jp.panta.misskeyandroidclient.model.notes.draft

import androidx.room.Ignore

data class DraftPoll(
    @Ignore var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
)