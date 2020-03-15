package jp.panta.misskeyandroidclient.viewmodel.core

import androidx.lifecycle.LiveData
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation

interface AccountStore {
    val currentAccount: LiveData<AccountRelation>
    val accounts: LiveData<List<AccountRelation>>

    fun add(account: Account)

    fun remove(account: Account)

    fun changeCurrent(account: Account)
}