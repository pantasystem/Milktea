package jp.panta.misskeyandroidclient.model.instance

import android.util.Log
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
                val remote = try{
                    metaStore.get(instanceDomain)
                }catch(e: Exception){
                    null
                }
                if(remote != null){
                    return try{
                        metaRepository.add(remote)
                    }catch(e: Exception){
                        Log.e("MediatorMetaStore", "データベースエラー", e)
                        remote
                    }
                }
            }
            return local
        }catch(e: Exception){
            return null
        }


    }

}