package jp.panta.misskeyandroidclient.model.instance.db

import androidx.room.Database
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMetaRepository(
    private val metaDAO: MetaDAO,
    private val emojiAliasDAO: EmojiAliasDAO,
    val database: DataBase
) : MetaRepository {

    override suspend fun add(meta: Meta): Meta {

        return database.runInTransaction<Meta>{
            metaDAO.insert(MetaDTO(meta))
            val emojiDTOList = meta.emojis?.map{
                EmojiDTO(it, meta.uri)
            }
            if(emojiDTOList != null){
                metaDAO.insertAll(emojiDTOList)
            }
            meta.emojis?.map { emoji ->
                emoji.aliases?.filter {
                    it.isNotBlank()
                }?.map { alias ->
                    EmojiAlias(
                        alias,
                        emoji.name,
                        meta.uri
                    )
                }?: emptyList()
            }?.forEach { list ->
                emojiAliasDAO.insertAll(list)
            }
            meta
        }
    }

    override suspend fun delete(meta: Meta) {
        metaDAO.delete(MetaDTO(meta))
    }

    override suspend fun get(instanceDomain: String): Meta? {
        return metaDAO.findByInstanceDomain(instanceDomain)?.toMeta()
    }

    override fun observe(instanceDomain: String): Flow<Meta?> {
        return metaDAO.observeByInstanceDomain(instanceDomain).map {
            it?.toMeta()
        }
    }
}