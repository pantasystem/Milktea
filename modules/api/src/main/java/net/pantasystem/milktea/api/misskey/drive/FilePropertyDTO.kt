package net.pantasystem.milktea.api.misskey.drive

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class FilePropertyDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class)
    val createdAt: Instant,

    @SerialName("name")
    val name: String,

    @SerialName("type")
    val type: String,

    @SerialName("md5")
    val md5: String,

    @SerialName("size")
    val size: Int? = null,

    @SerialName("userId")
    val userId: String? = null,

    @SerialName("folderId")
    val folderId: String? = null,

    @SerialName("comment")
    val comment: String? = null,

    @SerialName("isSensitive")
    val isSensitive: Boolean? = null,

    @SerialName("url")
    val url: String,

    @SerialName("thumbnailUrl")
    val thumbnailUrl: String? = null,

    @SerialName("attachedNoteIds")
    val attachedNoteIds: List<String>? = null,

    @SerialName("properties")
    val properties: Properties? = null,

    @SerialName("blurhash")
    val blurhash: String? = null,
)
    : Serializable {

    @kotlinx.serialization.Serializable
    data class Properties(
        @SerialName("width")
        val width: Float? = null,

        @SerialName("height")
        val height: Float? = null
    ) : Serializable

    fun getThumbnailUrl(instanceBaseUrl: String): String {
        return getUrl(instanceBaseUrl, thumbnailUrl ?: url)
    }

    fun getUrl(instanceBaseUrl: String): String {
        return getUrl(instanceBaseUrl, url)
    }

    fun getUrl(instanceBaseUrl: String, url: String): String {
        val hostUrl = if (instanceBaseUrl.endsWith("/")) {
            instanceBaseUrl.substring(0, instanceBaseUrl.length - 1)
        } else {
            instanceBaseUrl
        }

        return when {
            url.startsWith("https://") -> {
                url
            }
            url.startsWith("/") -> {
                hostUrl + url
            }
            else -> {
                "$hostUrl/$url"
            }
        }
    }


}