package net.pantasystem.milktea.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.auth.databinding.FragmentAuthResultBinding
import net.pantasystem.milktea.auth.viewmodel.AuthViewModel
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken

@FlowPreview
@ExperimentalCoroutinesApi
class AuthResultFragment : Fragment(){

    lateinit var binding: FragmentAuthResultBinding

    val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth_result, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.authorization.collect {
                    if(it is Authorization.Approved) {
                        if (it.accessToken is AccessToken.Misskey) {
                            binding.user = (it.accessToken as AccessToken.Misskey).user
                        }
                        binding.continueAuth.isEnabled = true
                    }
                }
            }
        }

        binding.continueAuth.setOnClickListener {
            viewModel.confirmApprove()
        }

    }


}