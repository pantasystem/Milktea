package jp.panta.misskeyandroidclient.viewmodel.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.users.User

class MainViewModel : ViewModel(){

    val test = MutableLiveData<String>()

    fun showProfile(user: User){

    }

}