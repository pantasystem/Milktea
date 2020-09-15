package jp.panta.misskeyandroidclient.model.instance.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import jp.panta.misskeyandroidclient.model.instance.Meta

@DatabaseView
class MetaRelation {

    @Embedded
    lateinit var meta: MetaDTO

    @Relation(parentColumn = "uri", entityColumn = "instanceDomain")
    lateinit var emojis: List<EmojiDTO>

    @Ignore
    fun toMeta(): Meta{
        return Meta(
            bannerUrl = this.meta.bannerUrl,
            cacheRemoteFiles = this.meta.cacheRemoteFiles,
            description = this.meta.description,
            disableGlobalTimeline = this.meta.disableGlobalTimeline,
            disableLocalTimeline = this.meta.disableLocalTimeline,
            disableRegistration = this.meta.disableRegistration,
            driveCapacityPerLocalUserMb = this.meta.driveCapacityPerLocalUserMb,
            driveCapacityPerRemoteUserMb = this.meta.driveCapacityPerRemoteUserMb,
            enableDiscordIntegration = this.meta.enableDiscordIntegration,
            enableEmail = this.meta.enableEmail,
            enableEmojiReaction = this.meta.enableEmojiReaction,
            enableGithubIntegration = this.meta.enableGithubIntegration,
            enableRecaptcha = this.meta.enableRecaptcha,
            enableServiceWorker = this.meta.enableServiceWorker,
            enableTwitterIntegration = this.meta.enableTwitterIntegration,
            errorImageUrl = this.meta.errorImageUrl,
            feedbackUrl = this.meta.feedbackUrl,
            iconUrl = this.meta.iconUrl,
            maintainerEmail = this.meta.maintainerEmail,
            maintainerName = this.meta.maintainerName,
            mascotImageUrl = this.meta.mascotImageUrl,
            maxNoteTextLength = this.meta.maxNoteTextLength,
            name = this.meta.name,
            recaptchaSiteKey = this.meta.recaptchaSiteKey,
            secure = this.meta.secure,
            swPublicKey = this.meta.swPublicKey,
            toSUrl = this.meta.toSUrl,
            version = this.meta.version,
            emojis = this.emojis.map{
                it.toEmoji()
            },
            uri = this.meta.uri
        )
    }
}