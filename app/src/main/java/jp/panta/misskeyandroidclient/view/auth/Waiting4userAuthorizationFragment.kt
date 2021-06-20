package jp.panta.misskeyandroidclient.view.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentWaiting4UserAuthorizationBinding
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class Waiting4userAuthorizationFragment : Fragment(R.layout.fragment_waiting_4_user_authorization) {

    private val binding: FragmentWaiting4UserAuthorizationBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miCore = context?.applicationContext as MiCore
        val authViewModel = ViewModelProvider(requireActivity(), AuthViewModel.Factory(miCore))[AuthViewModel::class.java]

        authViewModel.authorization.mapNotNull {
            it as? Authorization.Waiting4UserAuthorization
        }.onEach {
            Log.d("Waiting4Auth", "auth url: ${it.session.url}")
            binding.authenticationUrlViewEditText.setText(it.session.url)

        }.launchIn(lifecycleScope)

        binding.copyToClipboardButton.setOnClickListener {

        }

        binding.approvedButton.setOnClickListener {
            authViewModel.getAccessToken()
        }
    }
}