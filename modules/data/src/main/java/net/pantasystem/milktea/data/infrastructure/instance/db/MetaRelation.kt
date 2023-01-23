package net.pantasystem.milktea.data.infrastructure.instance.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import net.pantasystem.milktea.model.instance.Meta

@DatabaseView
class MetaRelation {

    @Embedded
    lateinit var meta: MetaDTO

    @Relation(parentColumn = "uri", entityColumn = "instanceDomain")
    lateinit var emojis: List<EmojiDTO>

    @Relation(parentColumn = "uri", entityColumn = "instanceDomain")
    lateinit var aliases: List<EmojiAlias>

    @Ignore
    fun toMeta(): Meta {
        val mapEmojis = aliases.groupBy {
            it.name to it.instanceDomain
        }
        val e = emojis.map { emoji ->
            val alias = mapEmojis[emoji.name to emoji.instanceDomain]?.map { a ->
                a.alias
            }?: emptyList()
            emoji.toEmoji(alias)

        }
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
            emojis = e,
            uri = this.meta.uri
        )
    }
}