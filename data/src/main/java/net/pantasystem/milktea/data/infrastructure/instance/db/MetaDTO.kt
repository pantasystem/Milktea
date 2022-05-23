package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import net.pantasystem.milktea.model.instance.Meta

@Entity(tableName = "meta_table")
data class MetaDTO(
    @PrimaryKey(autoGenerate = false) @SerializedName("uri") val uri: String,
    @SerializedName("bannerUrl") val bannerUrl: String?,
    @SerializedName("cacheRemoteFiles") val cacheRemoteFiles: Boolean?,
    @SerializedName("description") val description: String?,
    @SerializedName("disableGlobalTimeline") val disableGlobalTimeline: Boolean?,
    @SerializedName("disableLocalTimeline") val disableLocalTimeline: Boolean?,
    @SerializedName("disableRegistration") val disableRegistration: Boolean?,
    @SerializedName("driveCapacityPerLocalUserMb") val driveCapacityPerLocalUserMb: Int?,
    @SerializedName("driveCapacityPerRemoteUserMb") val driveCapacityPerRemoteUserMb: Int?,
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
    @SerializedName("maintainerEmail") val maintainerEmail: String?,
    @SerializedName("maintainerName") val maintainerName: String?,
    @SerializedName("mascotImageUrl") val mascotImageUrl: String?,
    @SerializedName("maxNoteTextLength") val maxNoteTextLength: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("recaptchaSiteKey") val recaptchaSiteKey: String?,
    @SerializedName("secure") val secure: Boolean?,
    @SerializedName("swPublickey") val swPublicKey: String?,
    @SerializedName("ToSUrl") val toSUrl: String?,
    @SerializedName("version") val version: String
){
    
    constructor(meta: Meta) : this(
        bannerUrl = meta.bannerUrl,
        cacheRemoteFiles = meta.cacheRemoteFiles,
        description = meta.description,
        disableGlobalTimeline = meta.disableGlobalTimeline,
        disableLocalTimeline = meta.disableLocalTimeline,
        disableRegistration = meta.disableRegistration,
        driveCapacityPerLocalUserMb = meta.driveCapacityPerLocalUserMb,
        driveCapacityPerRemoteUserMb = meta.driveCapacityPerRemoteUserMb,
        enableDiscordIntegration = meta.enableDiscordIntegration,
        enableEmail = meta.enableEmail,
        enableEmojiReaction = meta.enableEmojiReaction,
        enableGithubIntegration = meta.enableGithubIntegration,
        enableRecaptcha = meta.enableRecaptcha,
        enableServiceWorker = meta.enableServiceWorker,
        enableTwitterIntegration = meta.enableTwitterIntegration,
        errorImageUrl = meta.errorImageUrl,
        feedbackUrl = meta.feedbackUrl,
        iconUrl = meta.iconUrl,
        maintainerEmail = meta.maintainerEmail,
        maintainerName = meta.maintainerName,
        mascotImageUrl = meta.mascotImageUrl,
        maxNoteTextLength = meta.maxNoteTextLength,
        name = meta.name,
        recaptchaSiteKey = meta.recaptchaSiteKey,
        secure = meta.secure,
        swPublicKey = meta.swPublicKey,
        toSUrl = meta.toSUrl,
        version = meta.version,
        uri = meta.uri
    )


}