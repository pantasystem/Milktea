package net.pantasystem.milktea.messaging

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
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.messaging.viewmodel.MessageHistoryViewModel
import net.pantasystem.milktea.model.messaging.messagingId
import javax.inject.Inject


@AndroidEntryPoint
class MessagingHistoryFragment : Fragment() {

    private val historyViewModel: MessageHistoryViewModel by viewModels()

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

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
                                    val intent = userDetailNavigation.newIntent(
                                        UserDetailNavigationArgs.UserId(action.user.id)
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