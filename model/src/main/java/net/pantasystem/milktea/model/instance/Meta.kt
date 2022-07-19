package net.pantasystem.milktea.model.instance

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.reaction.Reaction

@Serializable
data class Meta(
    @SerialName("uri") @SerializedName("uri") var uri: String,
    @SerialName("bannerUrl") @SerializedName("bannerUrl") var bannerUrl: String? = null,
    @SerialName("cacheRemoteFiles") @SerializedName("cacheRemoteFiles") var cacheRemoteFiles: Boolean? = null,
    @SerialName("description") @SerializedName("description") var description: String? = null,
    @SerialName("disableGlobalTimeline") @SerializedName("disableGlobalTimeline") var disableGlobalTimeline: Boolean? = null,
    @SerialName("disableLocalTimeline") @SerializedName("disableLocalTimeline") var disableLocalTimeline: Boolean? = null,
    @SerialName("disableRegistration") @SerializedName("disableRegistration") var disableRegistration: Boolean? = null,
    @SerialName("driveCapacityPerLocalUserMb") @SerializedName("driveCapacityPerLocalUserMb") var driveCapacityPerLocalUserMb: Int? = null,
    @SerialName("driveCapacityPerRemoteUserMb") @SerializedName("driveCapacityPerRemoteUserMb") var driveCapacityPerRemoteUserMb: Int? = null,
    @SerialName("enableDiscordIntegration") @SerializedName("enableDiscordIntegration") var enableDiscordIntegration: Boolean? = null,
    @SerialName("enableEmail") @SerializedName("enableEmail") var enableEmail: Boolean? = null,
    @SerialName("enableEmojiReaction") @SerializedName("enableEmojiReaction") var enableEmojiReaction: Boolean? = null,
    @SerialName("enableGithubIntegration") @SerializedName("enableGithubIntegration") var enableGithubIntegration: Boolean? = null,
    @SerialName("enableRecaptcha") @SerializedName("enableRecaptcha") var enableRecaptcha: Boolean? = null,
    @SerialName("enableServiceWorker") @SerializedName("enableServiceWorker") var enableServiceWorker: Boolean? = null,
    @SerialName("enableTwitterIntegration") @SerializedName("enableTwitterIntegration") var enableTwitterIntegration: Boolean? = null,
    @SerialName("errorImageUrl") @SerializedName("errorImageUrl") var errorImageUrl: String? = null,
    @SerialName("feedbackUrl") @SerializedName("feedbackUrl") var feedbackUrl: String? = null,
    @SerialName("iconUrl") @SerializedName("iconUrl") var iconUrl: String? = null,
    @SerialName("maintainerEmail") @SerializedName("maintainerEmail") var maintainerEmail: String? = null,
    @SerialName("maintainerName") @SerializedName("maintainerName") var maintainerName: String? = null,
    @SerialName("mascotImageUrl") @SerializedName("mascotImageUrl") var mascotImageUrl: String? = null,
    @SerialName("maxNoteTextLength") @SerializedName("maxNoteTextLength") var maxNoteTextLength: Int? = null,
    @SerialName("name") @SerializedName("name") var name: String? = null,
    @SerialName("recaptchaSiteKey") @SerializedName("recaptchaSiteKey") var recaptchaSiteKey: String? = null,
    @SerialName("secure") @SerializedName("secure") var secure: Boolean? = null,
    @SerialName("swPublickey") @SerializedName("swPublickey") var swPublicKey: String? = null,
    @SerialName("ToSUrl") @SerializedName("ToSUrl") var toSUrl: String? = null,
    @SerialName("version") @SerializedName("version") var version: String = "",
    @SerialName("emojis") @SerializedName("emojis") var emojis: List<Emoji>? = null
){



    @Ignore
    fun getVersion(): Version{
        return Version(version)
    }


    fun isOwnEmojiBy(emoji: Reaction): Boolean {
        return emojis?.any {
            it.name == emoji.getName()
        } == true
    }
}