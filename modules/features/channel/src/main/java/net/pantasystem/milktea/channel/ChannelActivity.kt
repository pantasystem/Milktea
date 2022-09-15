package net.pantasystem.milktea.channel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.ChannelNavigation
import javax.inject.Inject


@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

    @Inject
    lateinit var accountStore: AccountStore
    private val channelViewModel: ChannelViewModel by viewModels()

    @Inject
    lateinit var setTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()

        setContent {
            MdcTheme {
                ChannelScreen(
                    onNavigateUp = {
                        finish()
                    },
                    accountStore = accountStore,
                    channelViewModel = channelViewModel
                )
            }
        }
    }
}


class ChannelNavigationImpl @Inject constructor(val activity: Activity): ChannelNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, ChannelActivity::class.java)
    }
}