package jp.panta.misskeyandroidclient.api.drive

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.Serializable
import java.util.*

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
    val properties: Properties? = null
): Serializable{

    @kotlinx.serialization.Serializable
    data class Properties(
        val width: Int? = null,
        val height: Int? = null
    ) : Serializable

    private fun getThumbnailUrl(instanceBaseUrl: String): String{
        return getUrl(instanceBaseUrl, thumbnailUrl?: url)
    }

    private fun getUrl(instanceBaseUrl: String): String{
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



    fun toFileProperty(account: Account): FileProperty {
        return FileProperty(
            id = FileProperty.Id(account.accountId, id),
            name = name,
            createdAt = createdAt,
            type = type,
            md5 = md5,
            size = size?: 0,
            userId = userId?.let { User.Id(account.accountId, userId) },
            folderId = folderId,
            comment = comment,
            isSensitive = isSensitive?: false,
            url = getUrl(account.instanceDomain),
            thumbnailUrl = getThumbnailUrl(account.instanceDomain),
            properties = properties?.let {
                FileProperty.Properties(it.width, it.height)
            }
        )
    }

}