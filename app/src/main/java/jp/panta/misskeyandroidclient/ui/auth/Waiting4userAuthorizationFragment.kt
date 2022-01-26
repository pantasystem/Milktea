package jp.panta.misskeyandroidclient.ui.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentWaiting4UserAuthorizationBinding
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

@FlowPreview
@ExperimentalCoroutinesApi
class Waiting4userAuthorizationFragment : Fragment(R.layout.fragment_waiting_4_user_authorization) {

    private val binding: FragmentWaiting4UserAuthorizationBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miCore = context?.applicationContext as MiCore
        val authViewModel = ViewModelProvider(requireActivity(), AuthViewModel.Factory(miCore))[AuthViewModel::class.java]

        // EditTextにフォーカスが当たるようにしたいが、編集はできないようにしたいためこのようにしています。
        binding.authenticationUrlViewEditText.setOnKeyListener { _, _, _ ->
            return@setOnKeyListener true
        }

        authViewModel.authorization.mapNotNull {
            it as? Authorization.Waiting4UserAuthorization
        }.onEach {
            Log.d("Waiting4Auth", "auth url: ${it.session.url}")
            binding.authenticationUrlViewEditText.setText(it.session.url)

        }.launchIn(lifecycleScope)

        binding.copyToClipboardButton.setOnClickListener {
            (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also { clipboardManager ->
                (authViewModel.authorization.value as? Authorization.Waiting4UserAuthorization)?.session?.url?.let {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("misskey auth url", it))
                    Toast.makeText(requireContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.approvedButton.setOnClickListener {
            authViewModel.getAccessToken()
        }
    }
}