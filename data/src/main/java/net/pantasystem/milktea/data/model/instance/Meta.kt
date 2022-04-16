package net.pantasystem.milktea.data.model.instance

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import net.pantasystem.milktea.data.model.api.Version
import net.pantasystem.milktea.data.model.emoji.Emoji

data class Meta(
    @SerializedName("uri") var uri: String,
    @SerializedName("bannerUrl") var bannerUrl: String? = null,
    @SerializedName("cacheRemoteFiles") var cacheRemoteFiles: Boolean? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("disableGlobalTimeline") var disableGlobalTimeline: Boolean? = null,
    @SerializedName("disableLocalTimeline") var disableLocalTimeline: Boolean? = null,
    @SerializedName("disableRegistration") var disableRegistration: Boolean? = null,
    @SerializedName("driveCapacityPerLocalUserMb") var driveCapacityPerLocalUserMb: Int? = null,
    @SerializedName("driveCapacityPerRemoteUserMb") var driveCapacityPerRemoteUserMb: Int? = null,
    @SerializedName("enableDiscordIntegration") var enableDiscordIntegration: Boolean? = null,
    @SerializedName("enableEmail") var enableEmail: Boolean? = null,
    @SerializedName("enableEmojiReaction") var enableEmojiReaction: Boolean? = null,
    @SerializedName("enableGithubIntegration") var enableGithubIntegration: Boolean? = null,
    @SerializedName("enableRecaptcha") var enableRecaptcha: Boolean? = null,
    @SerializedName("enableServiceWorker") var enableServiceWorker: Boolean? = null,
    @SerializedName("enableTwitterIntegration") var enableTwitterIntegration: Boolean? = null,
    @SerializedName("errorImageUrl") var errorImageUrl: String? = null,
    @SerializedName("feedbackUrl") var feedbackUrl: String? = null,
    @SerializedName("iconUrl") var iconUrl: String? = null,
    @SerializedName("maintainerEmail") var maintainerEmail: String? = null,
    @SerializedName("maintainerName") var maintainerName: String? = null,
    @SerializedName("mascotImageUrl") var mascotImageUrl: String? = null,
    @SerializedName("maxNoteTextLength") var maxNoteTextLength: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("recaptchaSiteKey") var recaptchaSiteKey: String? = null,
    @SerializedName("secure") var secure: Boolean? = null,
    @SerializedName("swPublickey") var swPublicKey: String? = null,
    @SerializedName("ToSUrl") var toSUrl: String? = null,
    @SerializedName("version") var version: String = "",
    @SerializedName("emojis") var emojis: List<Emoji>? = null
){



    @Ignore
    fun getVersion(): Version{
        return Version(version)
    }

}