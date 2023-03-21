package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.instance.Meta

@Entity(tableName = "meta_table")
data class MetaDTO(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "uri")
    val uri: String,

    @ColumnInfo(name = "bannerUrl")
    val bannerUrl: String?,

    @ColumnInfo(name = "cacheRemoteFiles")
    val cacheRemoteFiles: Boolean?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "disableGlobalTimeline")
    val disableGlobalTimeline: Boolean?,

    @ColumnInfo(name = "disableLocalTimeline")
    val disableLocalTimeline: Boolean?,

    @ColumnInfo(name = "disableRegistration")
    val disableRegistration: Boolean?,

    @ColumnInfo(name = "driveCapacityPerLocalUserMb")
    val driveCapacityPerLocalUserMb: Int?,

    @ColumnInfo(name = "driveCapacityPerRemoteUserMb")
    val driveCapacityPerRemoteUserMb: Int?,

    @ColumnInfo(name = "enableDiscordIntegration")
    val enableDiscordIntegration: Boolean?,

    @ColumnInfo(name = "enableEmail")
    val enableEmail: Boolean?,

    @ColumnInfo(name = "enableEmojiReaction")
    val enableEmojiReaction: Boolean?,

    @ColumnInfo(name = "enableGithubIntegration")
    val enableGithubIntegration: Boolean?,

    @ColumnInfo(name = "enableRecaptcha")
    val enableRecaptcha: Boolean?,

    @ColumnInfo(name = "enableServiceWorker")
    val enableServiceWorker: Boolean?,

    @ColumnInfo(name = "enableTwitterIntegration")
    val enableTwitterIntegration: Boolean?,

    @ColumnInfo(name = "errorImageUrl")
    val errorImageUrl: String?,

    @ColumnInfo(name = "feedbackUrl")
    val feedbackUrl: String?,

    @ColumnInfo(name = "iconUrl")
    val iconUrl: String?,

    @ColumnInfo(name = "maintainerEmail")
    val maintainerEmail: String?,

    @ColumnInfo(name = "maintainerName")
    val maintainerName: String?,

    @ColumnInfo(name = "mascotImageUrl")
    val mascotImageUrl: String?,

    @ColumnInfo(name = "maxNoteTextLength")
    val maxNoteTextLength: Int?,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "recaptchaSiteKey")
    val recaptchaSiteKey: String?,

    @ColumnInfo(name = "secure")
    val secure: Boolean?,

    @ColumnInfo(name = "swPublicKey")
    val swPublicKey: String?,

    @ColumnInfo(name = "toSUrl")
    val toSUrl: String?,

    @ColumnInfo(name = "version")
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