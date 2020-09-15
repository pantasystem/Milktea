package jp.panta.misskeyandroidclient.model.instance

interface MetaStore {

    suspend fun get(instanceDomain: String) : Meta?

}