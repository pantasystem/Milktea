package net.pantasystem.milktea.model.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    @SerialName("uri") var uri: String,
    @SerialName("bannerUrl") var bannerUrl: String? = null,
    @SerialName("cacheRemoteFiles") var cacheRemoteFiles: Boolean? = null,
    @SerialName("description") var description: String? = null,
    @SerialName("disableGlobalTimeline") var disableGlobalTimeline: Boolean? = null,
    @SerialName("disableLocalTimeline") var disableLocalTimeline: Boolean? = null,
    @SerialName("disableRegistration") var disableRegistration: Boolean? = null,
    @SerialName("driveCapacityPerLocalUserMb") var driveCapacityPerLocalUserMb: Int? = null,
    @SerialName("driveCapacityPerRemoteUserMb") var driveCapacityPerRemoteUserMb: Int? = null,
    @SerialName("enableDiscordIntegration") var enableDiscordIntegration: Boolean? = null,
    @SerialName("enableEmail") var enableEmail: Boolean? = null,
    @SerialName("enableEmojiReaction") var enableEmojiReaction: Boolean? = null,
    @SerialName("enableGithubIntegration") var enableGithubIntegration: Boolean? = null,
    @SerialName("enableRecaptcha") var enableRecaptcha: Boolean? = null,
    @SerialName("enableServiceWorker") var enableServiceWorker: Boolean? = null,
    @SerialName("enableTwitterIntegration") var enableTwitterIntegration: Boolean? = null,
    @SerialName("errorImageUrl") var errorImageUrl: String? = null,
    @SerialName("feedbackUrl") var feedbackUrl: String? = null,
    @SerialName("iconUrl") var iconUrl: String? = null,
    @SerialName("maintainerEmail") var maintainerEmail: String? = null,
    @SerialName("maintainerName") var maintainerName: String? = null,
    @SerialName("mascotImageUrl") var mascotImageUrl: String? = null,
    @SerialName("maxNoteTextLength") var maxNoteTextLength: Int? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("recaptchaSiteKey") var recaptchaSiteKey: String? = null,
    @SerialName("secure") var secure: Boolean? = null,
    @SerialName("swPublickey") var swPublicKey: String? = null,
    @SerialName("ToSUrl") var toSUrl: String? = null,
    @SerialName("version") var version: String = "",
) {


    fun getVersion(): Version {
        return Version(version)
    }


}