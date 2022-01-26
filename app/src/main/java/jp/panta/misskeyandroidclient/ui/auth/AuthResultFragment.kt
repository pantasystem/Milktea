package jp.panta.misskeyandroidclient.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentAuthResultBinding
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class AuthResultFragment : Fragment(){

    lateinit var binding: FragmentAuthResultBinding

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

        val miCore = context?.applicationContext as MiCore
        val viewModel = ViewModelProvider(requireActivity(), AuthViewModel.Factory(miCore))[AuthViewModel::class.java]
        lifecycleScope.launchWhenCreated {
            viewModel.authorization.collect {
                if(it is Authorization.Approved) {
                    binding.user = it.accessToken.user
                }
            }

        }

        binding.continueAuth.setOnClickListener {
            viewModel.confirmApprove()
        }

    }


}