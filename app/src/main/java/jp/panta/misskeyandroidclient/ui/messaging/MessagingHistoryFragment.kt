package jp.panta.misskeyandroidclient.ui.messaging

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MessageActivity
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.MessageHistoryViewModel
import net.pantasystem.milktea.model.messaging.messagingId


@AndroidEntryPoint
class MessagingHistoryFragment : Fragment() {

    private val historyViewModel: MessageHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyViewModel.loadGroupAndUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    MessageHistoryScreen(
                        historyViewModel = historyViewModel,
                        onAction = { action ->
                            when (action) {
                                is Action.OnAvatarIconClick -> {
                                    val intent = UserDetailActivity.newInstance(
                                        requireActivity(),
                                        action.user.id
                                    )
                                    startActivity(intent)
                                }
                                is Action.OnClick -> {
                                    val intent = Intent(activity, MessageActivity::class.java)
                                    intent.putExtra(
                                        MessageActivity.EXTRA_MESSAGING_ID,
                                        action.history.messagingId
                                    )
                                    startActivity(intent)
                                }
                            }
                        }
                    )
                }
            }
        }.rootView
    }


}