package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.User

data class AccountViewData(val user: MutableLiveData<User>, val accountRelation: AccountRelation)