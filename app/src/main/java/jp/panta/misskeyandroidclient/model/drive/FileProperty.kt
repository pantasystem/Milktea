package jp.panta.misskeyandroidclient.model.drive

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FileProperty(
    @SerializedName("id") val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String? = null,
    @SerializedName("md5") val md5: String? = null,
    @SerializedName("size") val size: Int? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("folderId") val folderId: String? = null,
    @SerializedName("comment") val comment: String? = null,
    //@JsonProperty("properties") val properties: Property? = null,
    @SerializedName("isSensitive") val isSensitive: Boolean? = null,
    @SerializedName("url") val url: String,
    //@SerializedName("webpublicUrl") val webPublicUrl: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("attachedNoteIds") val attachedNoteIds: List<String?>? = null
): Serializable