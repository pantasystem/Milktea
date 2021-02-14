package jp.panta.misskeyandroidclient.model.drive

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.file.File
import java.io.Serializable

@kotlinx.serialization.Serializable
data class FileProperty(
    val id: String,
    //@JsonProperty("createdAt") @JsonFormat(pattern = REMOTE_DATE_FORMAT) val createdAt: Date?,
    val name: String,
    val type: String? = null,
    val md5: String? = null,
    val size: Int? = null,
    val userId: String? = null,
    val folderId: String? = null,
    val comment: String? = null,
    //@JsonProperty("properties") val properties: Property? = null,
    val isSensitive: Boolean? = null,
    val url: String,
    //@SerializedName("webpublicUrl") val webPublicUrl: String? = null,
    val thumbnailUrl: String? = null,
    val attachedNoteIds: List<String>? = null
): Serializable{


    fun getThumbnailUrl(instanceBaseUrl: String): String{
        return getUrl(instanceBaseUrl, thumbnailUrl?: url)
    }

    fun getUrl(instanceBaseUrl: String): String{
        return getUrl(instanceBaseUrl, url)
    }

    private fun getUrl(instanceBaseUrl: String, url: String): String{
        val hostUrl = if(instanceBaseUrl.endsWith("/")){
            instanceBaseUrl.substring(0, instanceBaseUrl.length - 1)
        }else{
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

    fun toFile(instanceBaseUrl: String): File{
        val path = getUrl(instanceBaseUrl)
        val thumbnailUrl = getThumbnailUrl(instanceBaseUrl)

        return File(
            name,
            path,
            type,
            id,
            null,
            thumbnailUrl,
            isSensitive
        )
    }

}