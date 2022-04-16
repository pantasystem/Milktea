package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.data.model.account.AccountStore
import net.pantasystem.milktea.data.model.channel.ChannelPagingModel
import jp.panta.misskeyandroidclient.ui.channel.ChannelScreen
import jp.panta.misskeyandroidclient.ui.channel.ChannelViewModel
import javax.inject.Inject


@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

    @Inject
    lateinit var accountStore: AccountStore
    private val channelViewModel: ChannelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()

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