package jp.panta.misskeyandroidclient.model.notes.poll

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class CreatePoll(
    var choices: List<String>,
    val multiple: Boolean,
    val expiresAt: Long? = null
): JavaSerializable