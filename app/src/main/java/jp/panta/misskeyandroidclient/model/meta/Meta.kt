package jp.panta.misskeyandroidclient.model.meta

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.emoji.Emoji

data class Meta(
    @SerializedName("announcements") val announcements: List<Any?>?,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    @SerializedName("cacheRemoteFiles") val cacheRemoteFiles: Boolean?,
    @SerializedName("description") val description: String?,
    @SerializedName("disableGlobalTimeline") val disableGlobalTimeline: Boolean?,
    @SerializedName("disableLocalTimeline") val disableLocalTimeline: Boolean?,
    @SerializedName("disableRegistration") val disableRegistration: Boolean?,
    @SerializedName("driveCapacityPerLocalUserMb") val driveCapacityPerLocalUserMb: Int?,
    @SerializedName("driveCapacityPerRemoteUserMb") val driveCapacityPerRemoteUserMb: Int?,
    @SerializedName("emojis") val emojis: List<Emoji>?,
    @SerializedName("enableDiscordIntegration") val enableDiscordIntegration: Boolean?,
    @SerializedName("enableEmail") val enableEmail: Boolean?,
    @SerializedName("enableEmojiReaction") val enableEmojiReaction: Boolean?,
    @SerializedName("enableGithubIntegration") val enableGithubIntegration: Boolean?,
    @SerializedName("enableRecaptcha") val enableRecaptcha: Boolean?,
    @SerializedName("enableServiceWorker") val enableServiceWorker: Boolean?,
    @SerializedName("enableTwitterIntegration") val enableTwitterIntegration: Boolean?,
    @SerializedName("errorImageUrl") val errorImageUrl: String?,
    @SerializedName("feedbackUrl") val feedbackUrl: String?,
    @SerializedName("iconUrl") val iconUrl: String?,
    @SerializedName("langs") val langs: List<Any?>?,
    @SerializedName("machine") val machine: String?,
    @SerializedName("maintainerEmail") val maintainerEmail: String?,
    @SerializedName("maintainerName") val maintainerName: String?,
    @SerializedName("mascotImageUrl") val mascotImageUrl: String?,
    @SerializedName("maxNoteTextLength") val maxNoteTextLength: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("node") val node: String?,
    @SerializedName("os") val os: String?,
    @SerializedName("recaptchaSiteKey") val recaptchaSiteKey: String?,
    @SerializedName("repositoryUrl") val repositoryUrl: String?,
    @SerializedName("secure") val secure: Boolean?,
    @SerializedName("swPublickey") val swPublickey: String?,
    @SerializedName("ToSUrl") val toSUrl: String?,
    @SerializedName("uri") val uri: String?,
    @SerializedName("version") val version: String
){
    @Ignore
    fun getVersion(): Version{
        return Version(version)
    }

    val emojiNameMap: Map<String, Emoji> = emojis?.map{
        it.name to it
    }?.toMap()?: emptyMap()
}