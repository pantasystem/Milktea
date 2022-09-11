package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.instance.Meta

@Entity(tableName = "meta_table")
data class MetaDTO(
    @PrimaryKey(autoGenerate = false) val uri: String,
    val bannerUrl: String?,
    val cacheRemoteFiles: Boolean?,
    val description: String?,
    val disableGlobalTimeline: Boolean?,
    val disableLocalTimeline: Boolean?,
    val disableRegistration: Boolean?,
    val driveCapacityPerLocalUserMb: Int?,
    val driveCapacityPerRemoteUserMb: Int?,
    val enableDiscordIntegration: Boolean?,
    val enableEmail: Boolean?,
    val enableEmojiReaction: Boolean?,
    val enableGithubIntegration: Boolean?,
    val enableRecaptcha: Boolean?,
    val enableServiceWorker: Boolean?,
    val enableTwitterIntegration: Boolean?,
    val errorImageUrl: String?,
    val feedbackUrl: String?,
    val iconUrl: String?,
    val maintainerEmail: String?,
    val maintainerName: String?,
    val mascotImageUrl: String?,
    val maxNoteTextLength: Int?,
    val name: String?,
    val recaptchaSiteKey: String?,
    val secure: Boolean?,
    val swPublicKey: String?,
    val toSUrl: String?,
    val version: String
) {

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