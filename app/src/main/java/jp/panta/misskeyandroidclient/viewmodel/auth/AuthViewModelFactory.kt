package jp.panta.misskeyandroidclient.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == AuthViewModel::class.java)
            return AuthViewModel() as T

        throw IllegalArgumentException("error")
    }
}