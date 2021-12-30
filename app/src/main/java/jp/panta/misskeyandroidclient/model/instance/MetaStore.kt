package jp.panta.misskeyandroidclient.model.instance

interface MetaStore {

    suspend fun fetch(instanceDomain: String) : Meta

}