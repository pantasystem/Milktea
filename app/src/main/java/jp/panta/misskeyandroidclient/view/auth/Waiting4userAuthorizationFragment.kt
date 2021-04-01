package jp.panta.misskeyandroidclient.view.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentWaiting4UserAuthorizationBinding
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel

class Waiting4userAuthorizationFragment : Fragment() {

    lateinit var binding: FragmentWaiting4UserAuthorizationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_waiting_4_user_authorization, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miCore = context?.applicationContext as MiCore
        val authViewModel = ViewModelProvider(requireActivity(), AuthViewModel.Factory(miCore))[AuthViewModel::class.java]

        binding.approvedButton.setOnClickListener {
            authViewModel.getAccessToken()
        }
    }
}