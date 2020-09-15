package jp.panta.misskeyandroidclient.model.instance

import java.util.concurrent.ConcurrentHashMap

class MediatorMetaStore(
    val metaRepository : MetaRepository,
    val metaStore: MetaStore,
    val isUpdateRepository: Boolean
) : MetaStore{

    override suspend fun get(instanceDomain: String): Meta? {
        try{
            val local = metaRepository.get(instanceDomain)
            if(local == null || isUpdateRepository){
                val remote = metaStore.get(instanceDomain)
                if(remote != null){
                    return metaRepository.add(remote)
                }
            }
        }catch(e: Exception){
            return null
        }

        return null

    }

}