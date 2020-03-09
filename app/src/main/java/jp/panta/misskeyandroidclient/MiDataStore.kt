package jp.panta.misskeyandroidclient

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance

interface MiDataStore {
    fun switchCurrentAccount()

    fun addAccount(ci: ConnectionInstance)
}