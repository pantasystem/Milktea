package net.pantasystem.milktea.messaging

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.messaging.databinding.FragmentMessagingHistoryBinding
import net.pantasystem.milktea.messaging.viewmodel.MessageHistoryViewModel
import net.pantasystem.milktea.model.messaging.messagingId
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject


@AndroidEntryPoint
class MessagingHistoryFragment : Fragment(R.layout.fragment_messaging_history) {

    private val historyViewModel: MessageHistoryViewModel by viewModels()

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    internal lateinit var configRepository: LocalConfigRepository


    val binding: FragmentMessagingHistoryBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyViewModel.loadGroupAndUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeBase.apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
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
        }
    }


}