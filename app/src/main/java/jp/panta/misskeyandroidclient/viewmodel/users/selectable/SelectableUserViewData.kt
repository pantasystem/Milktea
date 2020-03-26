package jp.panta.misskeyandroidclient.viewmodel.users.selectable

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData

data class SelectableUserViewData(
    val user: UserViewData,
    val isSelected: Boolean = false
)