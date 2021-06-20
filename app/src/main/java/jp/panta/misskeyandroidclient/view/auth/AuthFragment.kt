package jp.panta.misskeyandroidclient.view.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentAppAuthBinding
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import jp.panta.misskeyandroidclient.viewmodel.auth.app.AppAuthViewModel

class AuthFragment : Fragment(){

    lateinit var binding: FragmentAppAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_auth, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miCore = context?.applicationContext as MiCore
        val appAuthViewModel = ViewModelProvider(this, AppAuthViewModel.Factory(CustomAuthStore.newInstance(requireContext()), miCore))[AppAuthViewModel::class.java]
        appAuthViewModel.appName.value = getString(R.string.app_name)

        val authViewModel = ViewModelProvider(requireActivity(), AuthViewModel.Factory(miCore))[AuthViewModel::class.java]
        binding.lifecycleOwner = this
        binding.appAuthViewModel = appAuthViewModel
        appAuthViewModel.waiting4UserAuthorization.observe(viewLifecycleOwner) {
            it?.let {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.session.url)))
                authViewModel.setState(it)
                appAuthViewModel.waiting4UserAuthorization.postValue(null)
            }
        }

        appAuthViewModel.app.observe(viewLifecycleOwner) { app ->
            if (app != null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.successfully_created_the_app) + " ${app.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}