package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import net.pantasystem.milktea.model.drive.UpdateFileProperty
import net.pantasystem.milktea.model.drive.ValueType

//@Serializable
//data class UpdateFileDTO(
//    @SerialName("i")
//    val i: String,
//
//    @SerialName("fileId")
//    val fileId: String,
//
//    @SerialName("folderId")
//    val folderId: String?,
//
//    @SerialName("name")
//    val name: String,
//
//    @SerialName("comment")
//    val comment: String?,
//
//    @SerialName("isSensitive")
//    val isSensitive: Boolean,
//) {
//    companion object
//}

//fun UpdateFileDTO.Companion.from(token: String, model: UpdateFileProperty): UpdateFileDTO {
//    return UpdateFileDTO(
//        i = token,
//        comment = model.comment,
//        fileId = model.fileId.fileId,
//        folderId = model.folderId,
//        isSensitive = model.isSensitive,
//        name = model.name
//    )
//}

@OptIn(ExperimentalSerializationApi::class)
fun UpdateFileProperty.toJsonObject(token: String): JsonObject {
    return buildJsonObject {
        put("i", JsonPrimitive(token))
        put("fileId", JsonPrimitive(fileId.fileId))

        when(val v = comment) {
            is ValueType.Empty -> put("comment", JsonPrimitive(null))
            is ValueType.Some -> put("comment", JsonPrimitive(v.value))
            null -> Unit
        }

        when(val v = folderId) {
            is ValueType.Empty -> put("folderId", JsonPrimitive(null))
            is ValueType.Some -> put("folderId", JsonPrimitive(v.value))
            null -> Unit
        }

        when(val v = name) {
            is ValueType.Empty -> put("name", JsonPrimitive(null))
            is ValueType.Some -> put("name", JsonPrimitive(v.value))
            null -> Unit
        }

        when(val v = isSensitive) {
            is ValueType.Empty -> put("isSensitive", JsonPrimitive(null))
            is ValueType.Some -> put("isSensitive", JsonPrimitive(v.value))
            null -> Unit
        }

    }
}