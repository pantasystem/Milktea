package net.pantasystem.milktea.data.infrastructure.emoji.objectbox

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectBoxCustomEmojiRecordDAO @Inject constructor(
    val boxStore: BoxStore
) {

    private val customEmojiBoxStore: Box<CustomEmojiRecord> by lazy {
        boxStore.boxFor()
    }


}