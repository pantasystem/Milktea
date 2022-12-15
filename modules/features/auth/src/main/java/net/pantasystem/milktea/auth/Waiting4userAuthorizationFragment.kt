package net.pantasystem.milktea.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.auth.databinding.FragmentWaiting4UserAuthorizationBinding
import net.pantasystem.milktea.auth.viewmodel.app.AppAuthViewModel
import net.pantasystem.milktea.data.infrastructure.auth.Authorization

@FlowPreview
@ExperimentalCoroutinesApi
class Waiting4userAuthorizationFragment : Fragment(R.layout.fragment_waiting_4_user_authorization) {

    private val binding: FragmentWaiting4UserAuthorizationBinding by dataBinding()

    private val viewModel: AppAuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // EditTextにフォーカスが当たるようにしたいが、編集はできないようにしたいためこのようにしています。
        binding.authenticationUrlViewEditText.setOnKeyListener { _, _, _ ->
            return@setOnKeyListener true
        }

        viewModel.state.mapNotNull {
            it.stateType as? Authorization.Waiting4UserAuthorization
        }.onEach {
            when (it) {
                is Authorization.Waiting4UserAuthorization.Mastodon -> {

                }
                is Authorization.Waiting4UserAuthorization.Misskey -> {
                    Log.d("Waiting4Auth", "auth url: ${it.session.url}")
                    binding.authenticationUrlViewEditText.setText(it.session.url)
                }
            }


        }.launchIn(lifecycleScope)

        binding.copyToClipboardButton.setOnClickListener {
            (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also { clipboardManager ->
                (viewModel.state.value.stateType as? Authorization.Waiting4UserAuthorization.Misskey)?.session?.url?.let {
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("misskey auth url", it))
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.copied_to_clipboard),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.approvedButton.setOnClickListener {
            viewModel.getAccessToken()
        }
    }
}