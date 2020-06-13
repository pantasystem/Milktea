package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData

data class AccountViewData(
    val accountRelation: AccountRelation
) : UserViewData(accountRelation.account.id)