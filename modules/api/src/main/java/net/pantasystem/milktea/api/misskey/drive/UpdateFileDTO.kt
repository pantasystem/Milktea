package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.drive.UpdateFileProperty

@Serializable
data class UpdateFileDTO(
    @SerialName("i")
    val i: String,

    @SerialName("fileId")
    val fileId: String,

    @SerialName("folderId")
    val folderId: String?,

    @SerialName("name")
    val name: String,

    @SerialName("comment")
    val comment: String?,

    @SerialName("isSensitive")
    val isSensitive: Boolean,
) {
    companion object
}

fun UpdateFileDTO.Companion.from(token: String, model: UpdateFileProperty): UpdateFileDTO {
    return UpdateFileDTO(
        i = token,
        comment = model.comment,
        fileId = model.fileId.fileId,
        folderId = model.folderId,
        isSensitive = model.isSensitive,
        name = model.name
    )
}