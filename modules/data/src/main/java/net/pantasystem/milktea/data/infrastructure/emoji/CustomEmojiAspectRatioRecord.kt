package net.pantasystem.milktea.data.infrastructure.emoji

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatio

@Entity
data class CustomEmojiAspectRatioRecord (
    @Id var id: Long = 0,
    @Unique
    var uri: String = "",
    var aspectRatio: Float = 1f,
) {

    companion object {
        fun from(model: CustomEmojiAspectRatio): CustomEmojiAspectRatioRecord {
            return CustomEmojiAspectRatioRecord(
                uri = model.uri,
                aspectRatio = model.aspectRatio
            )
        }
    }
}