package net.pantasystem.milktea.api.misskey.drive

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
data class FilePropertyDTO(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val name: String,
    val type: String,
    val md5: String,
    val size: Int? = null,
    val userId: String? = null,
    val folderId: String? = null,
    val comment: String? = null,
    val isSensitive: Boolean? = null,
    val url: String,
    val thumbnailUrl: String? = null,
    val attachedNoteIds: List<String>? = null,
    val properties: Properties? = null,
    val blurhash: String? = null,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class Properties(
        val width: Float? = null,
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