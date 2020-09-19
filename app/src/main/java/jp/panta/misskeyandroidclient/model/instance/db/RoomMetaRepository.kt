package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository

class RoomMetaRepository(
    val metaDAO: MetaDAO
) : MetaRepository {

    override suspend fun add(meta: Meta): Meta {
        metaDAO.insert(MetaDTO(meta))
        val emojiDTOList = meta.emojis?.map{
            EmojiDTO(it, meta.uri)
        }
        if(emojiDTOList != null){
            metaDAO.insertAll(emojiDTOList)
        }

        return meta
    }

    override suspend fun delete(meta: Meta) {
        metaDAO.delete(MetaDTO(meta))
    }

    override suspend fun get(instanceDomain: String): Meta? {
        return metaDAO.findByInstanceDomain(instanceDomain)?.toMeta()
    }
}