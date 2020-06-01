package jp.panta.misskeyandroidclient.model.notes.draft

data class DraftPoll(
    var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
)