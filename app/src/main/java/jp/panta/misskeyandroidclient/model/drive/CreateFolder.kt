package jp.panta.misskeyandroidclient.model.drive

import kotlinx.serialization.Serializable

@Serializable
data class CreateFolder(
    val i: String,
    val name: String,
    val parentId: String?
)