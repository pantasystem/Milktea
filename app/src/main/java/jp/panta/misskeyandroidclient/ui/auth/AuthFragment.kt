package jp.panta.misskeyandroidclient.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentAppAuthBinding
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.AuthViewModel
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.app.AppAuthViewModel
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.app.AuthErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthFragment : Fragment(){

    lateinit var binding: FragmentAppAuthBinding

    private val appAuthViewModel: AppAuthViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

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

        appAuthViewModel.appName.value = getString(R.string.app_name)

        binding.lifecycleOwner = this
        binding.appAuthViewModel = appAuthViewModel
        appAuthViewModel.waiting4UserAuthorization.observe(viewLifecycleOwner) {
            it?.let {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.generateAuthUrl())))
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
        lifecycleScope.launchWhenResumed {
            appAuthViewModel.errors.collect {
                binding.errorMsgView.visibility = if(it == null) View.GONE else View.VISIBLE
                if(it != null) {
                    binding.errorMsgView.text = when(it) {
                        is AuthErrors.GetMetaError -> {
                            getString(R.string.warning_s, it.throwable.toString())
                        }
                        is AuthErrors.GenerateTokenError -> {
                            getString(R.string.error_s, it.throwable.toString())
                        }
                    }
                }
            }
        }

    }
}