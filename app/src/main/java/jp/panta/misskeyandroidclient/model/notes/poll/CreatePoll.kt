package jp.panta.misskeyandroidclient.model.notes.poll

data class CreatePoll(
    val choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
)