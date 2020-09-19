package jp.panta.misskeyandroidclient.model.instance

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.emoji.Emoji

data class Meta(
    @SerializedName("uri") var uri: String,
    @SerializedName("bannerUrl") var bannerUrl: String?,
    @SerializedName("cacheRemoteFiles") var cacheRemoteFiles: Boolean?,
    @SerializedName("description") var description: String?,
    @SerializedName("disableGlobalTimeline") var disableGlobalTimeline: Boolean?,
    @SerializedName("disableLocalTimeline") var disableLocalTimeline: Boolean?,
    @SerializedName("disableRegistration") var disableRegistration: Boolean?,
    @SerializedName("driveCapacityPerLocalUserMb") var driveCapacityPerLocalUserMb: Int?,
    @SerializedName("driveCapacityPerRemoteUserMb") var driveCapacityPerRemoteUserMb: Int?,
    @SerializedName("enableDiscordIntegration") var enableDiscordIntegration: Boolean?,
    @SerializedName("enableEmail") var enableEmail: Boolean?,
    @SerializedName("enableEmojiReaction") var enableEmojiReaction: Boolean?,
    @SerializedName("enableGithubIntegration") var enableGithubIntegration: Boolean?,
    @SerializedName("enableRecaptcha") var enableRecaptcha: Boolean?,
    @SerializedName("enableServiceWorker") var enableServiceWorker: Boolean?,
    @SerializedName("enableTwitterIntegration") var enableTwitterIntegration: Boolean?,
    @SerializedName("errorImageUrl") var errorImageUrl: String?,
    @SerializedName("feedbackUrl") var feedbackUrl: String?,
    @SerializedName("iconUrl") var iconUrl: String?,
    @SerializedName("maintainerEmail") var maintainerEmail: String?,
    @SerializedName("maintainerName") var maintainerName: String?,
    @SerializedName("mascotImageUrl") var mascotImageUrl: String?,
    @SerializedName("maxNoteTextLength") var maxNoteTextLength: Int?,
    @SerializedName("name") var name: String?,
    @SerializedName("recaptchaSiteKey") var recaptchaSiteKey: String?,
    @SerializedName("secure") var secure: Boolean?,
    @SerializedName("swPublickey") var swPublicKey: String?,
    @SerializedName("ToSUrl") var toSUrl: String?,
    @SerializedName("version") var version: String,
    @SerializedName("emojis") var emojis: List<Emoji>?
){



    @Ignore
    fun getVersion(): Version{
        return Version(version)
    }

    @Ignore
    var emojiNameMap: Map<String, Emoji> = emojis?.map{
        it.name to it
    }?.toMap()?: emptyMap()
}