package net.pantasystem.milktea.data.model.instance

import net.pantasystem.milktea.common.Logger
import java.lang.IllegalStateException

class MediatorFetchMeta(
    private val metaRepository : MetaRepository,
    private val fetchMeta: FetchMeta,
    val loggerFactory: Logger.Factory,
    ) : FetchMeta{

    val logger: Logger by lazy {
        loggerFactory.create("MediatorMetaStore")
    }

    override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean): Meta {
        try{
            val local = metaRepository.get(instanceDomain)
            var remoteError: Throwable? = null
            if(local == null || isForceFetch){
                val remote = try{
                    fetchMeta.fetch(instanceDomain)
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