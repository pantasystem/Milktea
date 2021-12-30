package jp.panta.misskeyandroidclient.model.instance

import jp.panta.misskeyandroidclient.Logger
import java.lang.IllegalStateException

class MediatorMetaStore(
    private val metaRepository : MetaRepository,
    private val metaStore: MetaStore,
    private val isUpdateRepository: Boolean,
    val loggerFactory: Logger.Factory,
    ) : MetaStore{

    val logger: Logger by lazy {
        loggerFactory.create("MediatorMetaStore")
    }

    override suspend fun fetch(instanceDomain: String): Meta {
        try{
            val local = metaRepository.get(instanceDomain)
            var remoteError: Throwable? = null
            if(local == null || isUpdateRepository){
                val remote = try{
                    metaStore.fetch(instanceDomain)
                }catch(e: Exception){
                    remoteError = e
                    null
                }
                if(remote != null){
                    return try{
                        metaRepository.add(remote)
                    }catch(e: Exception){
                        throw e
                    }
                }
            }
            if(local != null) {
                return local
            }else if(remoteError != null) {
                throw remoteError
            }
            throw IllegalStateException("Metaの取得に失敗")
        }catch(e: Exception){
            throw e
        }


    }

}