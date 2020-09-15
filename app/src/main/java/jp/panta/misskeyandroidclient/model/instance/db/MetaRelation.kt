package jp.panta.misskeyandroidclient.model.instance.db

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import jp.panta.misskeyandroidclient.model.instance.Meta

@DatabaseView
class MetaRelation {

    @Embedded
    lateinit var meta: Meta

    @Relation(parentColumn = "uri", entityColumn = "instanceDomain")
    lateinit var emojis: List<EmojiDTO>

    @Ignore
    fun toMeta(): Meta{
        return meta.copy(
            emojis = emojis.map{
                it.toEmoji()
            }
        )
    }
}