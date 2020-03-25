package jp.panta.misskeyandroidclient.viewmodel.users.selectable

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.User

data class SelectableUserViewData(
    val user: User,
    val isSelected: Boolean = false
)