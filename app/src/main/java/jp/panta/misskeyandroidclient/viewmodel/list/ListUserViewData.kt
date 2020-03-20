package jp.panta.misskeyandroidclient.viewmodel.list

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.User

class ListUserViewData (val userId: String){
    val user = MutableLiveData<User>()

    fun accept(user: User){
        this.user.postValue(user)
    }
}