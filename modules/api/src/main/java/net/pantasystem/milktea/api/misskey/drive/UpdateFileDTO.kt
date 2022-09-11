package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.drive.UpdateFileProperty

@Serializable
data class UpdateFileDTO (
    val i: String,
    val fileId: String,
    val folderId: String?,
    val name: String,
    val comment: String?,
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