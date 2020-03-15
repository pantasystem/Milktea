package jp.panta.misskeyandroidclient.viewmodel.account

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.User

data class AccountViewData(val user: User, val accountRelation: AccountRelation)