package net.pantasystem.milktea.model.instance

import net.pantasystem.milktea.common.Logger

class MediatorFetchMeta(
    private val metaDataSource : MetaDataSource,
    private val fetchMeta: FetchMeta,
    val loggerFactory: Logger.Factory,
    ) : FetchMeta {

    val logger: Logger by lazy {
        loggerFactory.create("MediatorMetaStore")
    }

    override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean): Meta {
        try{
            val local = metaDataSource.get(instanceDomain)
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
                        metaDataSource.add(remote)
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