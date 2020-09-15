package jp.panta.misskeyandroidclient.model.instance

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.emoji.Emoji

@Entity(tableName = "meta_table")
data class Meta(
    @PrimaryKey(autoGenerate = false) @SerializedName("uri") var uri: String,
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
    @Ignore @SerializedName("emojis") var emojis: List<Emoji>?
){

    constructor(
        uri: String,
        bannerUrl: String?,
        cacheRemoteFiles: Boolean?,
        description: String,
        disableGlobalTimeline: Boolean?,
        disableLocalTimeline: Boolean?,
        disableRegistration: Boolean?,
        driveCapacityPerLocalUserMb: Int?,
        driveCapacityPerRemoteUserMb: Int?,
        enableDiscordIntegration: Boolean?,
        enableEmail: Boolean?,
        enableEmojiReaction: Boolean?,
        enableGithubIntegration: Boolean?,
        enableRecaptcha: Boolean?,
        enableServiceWorker: Boolean?,
        enableTwitterIntegration: Boolean?,
        errorImageUrl: String?,
        feedbackUrl: String?,
        iconUrl: String?,
        maintainerEmail: String?,
        maintainerName: String?,
        mascotImageUrl: String?,
        maxNoteTextLength: Int?,
        name: String?,
        recaptchaSiteKey: String?,
        secure: Boolean?,
        swPublicKey: String?,
        toSUrl: String?,
        version: String
    ) : this(
        bannerUrl = bannerUrl,
        cacheRemoteFiles = cacheRemoteFiles,
        description = description,
        disableGlobalTimeline = disableGlobalTimeline,
        disableLocalTimeline = disableLocalTimeline,
        disableRegistration = disableRegistration,
        driveCapacityPerLocalUserMb = driveCapacityPerLocalUserMb,
        driveCapacityPerRemoteUserMb = driveCapacityPerRemoteUserMb,
        enableDiscordIntegration = enableDiscordIntegration,
        enableEmail = enableEmail,
        enableEmojiReaction = enableEmojiReaction,
        enableGithubIntegration = enableGithubIntegration,
        enableRecaptcha = enableRecaptcha,
        enableServiceWorker = enableServiceWorker,
        enableTwitterIntegration = enableTwitterIntegration,
        errorImageUrl = errorImageUrl,
        feedbackUrl = feedbackUrl,
        iconUrl = iconUrl,
        maintainerEmail = maintainerEmail,
        maintainerName = maintainerName,
        mascotImageUrl = mascotImageUrl,
        maxNoteTextLength = maxNoteTextLength,
        name = name,
        recaptchaSiteKey = recaptchaSiteKey,
        secure = secure,
        swPublicKey = swPublicKey,
        toSUrl = toSUrl,
        version = version,
        emojis = emptyList(),
        uri = uri
        )


    fun getVersion(): Version{
        return Version(version)
    }

    @Ignore
    var emojiNameMap: Map<String, Emoji> = emojis?.map{
        it.name to it
    }?.toMap()?: emptyMap()
}