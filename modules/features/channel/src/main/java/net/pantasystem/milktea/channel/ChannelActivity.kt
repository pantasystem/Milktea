package net.pantasystem.milktea.channel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_navigation.ChannelNavigation
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject


@AndroidEntryPoint
class ChannelActivity : AppCompatActivity() {

    @Inject
    internal lateinit var accountStore: AccountStore

    @Inject
    internal lateinit var setTheme: ApplyTheme

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    internal lateinit var settingStore: SettingStore


    private val channelViewModel: ChannelViewModel by viewModels()

    private val confirmViewModel: ConfirmViewModel by viewModels()

    private val notesViewModel: NotesViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()

        ActionNoteHandler(
            settingStore = settingStore,
            activity = this,
            mNotesViewModel = notesViewModel,
            confirmViewModel = confirmViewModel
        ).initViewModelListener()

        setContent {
            val navController = rememberNavController()
            MdcTheme {
                NavHost(navController = navController, startDestination = "/channels") {
                    composable("/channels") {
                        ChannelScreen(
                            onNavigateUp = {
                                finish()
                            },
                            accountStore = accountStore,
                            channelViewModel = channelViewModel,
                            onNavigateChannelDetail = {
                                navController.navigate("/accounts/${it.accountId}/channels/${it.channelId}")
                            }
                        )
                    }
                    composable(
                        "/accounts/{${ChannelDetailArgs.accountId}}/channels/{${ChannelDetailArgs.channelId}}",
                        arguments = listOf(
                            navArgument(ChannelDetailArgs.accountId) {
                                type = NavType.LongType
                            },
                            navArgument(ChannelDetailArgs.channelId) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        val viewModel: ChannelDetailViewModel = hiltViewModel()
                        val channel by viewModel.channel.collectAsState()
                        ChannelDetailScreen(
                            onNavigateUp = { navController.popBackStack() },
                            channelId = viewModel.channelId,
                            channel = channel,
                            onNavigateNoteEditor = {
                                startActivity(
                                    NoteEditorActivity.newBundle(
                                        this@ChannelActivity,
                                        channelId = it
                                    )
                                )
                            },
                            onUpdateFragment = { id, layout, channelId ->
                                val fragment = pageableFragmentFactory.create(
                                    Pageable.ChannelTimeline(channelId.channelId)
                                )
                                val ft = supportFragmentManager.beginTransaction()
                                ft.replace(id, fragment)
                                ft.commit()
                            }
                        )
                    }
                }
            }
        }
    }
}


class ChannelNavigationImpl @Inject constructor(val activity: Activity) : ChannelNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, ChannelActivity::class.java)
    }
}