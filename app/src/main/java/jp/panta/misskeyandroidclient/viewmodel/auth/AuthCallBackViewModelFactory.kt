package jp.panta.misskeyandroidclient.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class AuthCallBackViewModelFactory : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == AuthCallBackViewModel::class.java)
            return AuthCallBackViewModel() as T

        throw IllegalArgumentException("error")
    }
}