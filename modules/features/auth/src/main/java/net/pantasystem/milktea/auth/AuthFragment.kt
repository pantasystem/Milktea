package net.pantasystem.milktea.auth

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.auth.databinding.FragmentAppAuthBinding
import net.pantasystem.milktea.auth.viewmodel.AuthViewModel
import net.pantasystem.milktea.auth.viewmodel.app.AppAuthViewModel
import net.pantasystem.milktea.auth.viewmodel.app.AuthErrors

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AuthFragment : Fragment() {

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
                if (appAuthViewModel.isOpenInWebView.value) {
                    startActivity(
                        Intent(
                            requireContext(),
                            WebViewAuthActivity::class.java
                        ).also { intent ->
                            intent.putExtra(EXTRA_AUTH_URL, it.generateAuthUrl())
                            intent.putExtra(EXTRA_USERNAME, appAuthViewModel.username.value)
                        })
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.generateAuthUrl())))

                }
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                appAuthViewModel.errors.collect {
                    binding.errorMsgView.visibility = if (it == null) View.GONE else View.VISIBLE
                    if (it != null) {
                        binding.errorMsgView.text = when (it) {
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
}