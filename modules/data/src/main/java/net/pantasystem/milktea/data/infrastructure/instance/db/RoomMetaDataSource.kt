package net.pantasystem.milktea.data.infrastructure.instance.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource

class RoomMetaDataSource(
    private val metaDAO: MetaDAO,
    private val emojiAliasDAO: EmojiAliasDAO,
    val database: DataBase
) : MetaDataSource {

    override suspend fun add(meta: Meta): Meta {
        return database.runInTransaction<Meta>{
            val isExists = metaDAO.findByInstanceDomain(meta.uri) != null
            if (isExists) {
                metaDAO.update(MetaDTO(meta))
            } else {
                metaDAO.delete(MetaDTO(meta))
                metaDAO.insert(MetaDTO(meta))
            }

            val emojiDTOList = meta.emojis?.map{
                EmojiDTO(it, meta.uri)
            }
            if(emojiDTOList != null){
                metaDAO.deleteEmojisBy(meta.uri)
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