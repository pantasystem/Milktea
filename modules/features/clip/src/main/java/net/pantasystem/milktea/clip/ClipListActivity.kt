package net.pantasystem.milktea.clip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.ClipDetailNavigation
import net.pantasystem.milktea.common_navigation.ClipListNavigation
import net.pantasystem.milktea.common_navigation.ClipListNavigationArgs
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class ClipListActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var clipDetailNavigation: ClipDetailNavigation

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    private val mode: ClipListNavigationArgs.Mode by lazy {
        intent.getStringExtra(ClipListNavigationImpl.EXTRA_MODE)?.let {
            ClipListNavigationArgs.Mode.valueOf(it)
        } ?: ClipListNavigationArgs.Mode.View
    }

    private val clipListViewModel: ClipListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyTheme()

        setContent {
            val uiState by clipListViewModel.uiState.collectAsState()

            MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                ClipListScreen(
                    uiState = uiState,
                    mode = mode,
                    onClipTileClicked = {
                        when(mode) {
                            ClipListNavigationArgs.Mode.AddToTab -> {
                                clipListViewModel.onClipTileClicked(it)
                            }
                            ClipListNavigationArgs.Mode.View -> {
                                startActivity(clipDetailNavigation.newIntent(it.clip.id))
                            }
                        }
                    },
                    onToggleAddToTabButtonClicked = clipListViewModel::onToggleAddToTabButtonClicked,
                    onNavigateUp = {
                        finish()
                    }
                )
            }
        }
    }


}

class ClipListNavigationImpl @Inject constructor(
    private val activity: Activity
) : ClipListNavigation {
    companion object {
        const val EXTRA_ACCOUNT_ID = "ClipListActivity.EXTRA_ACCOUNT_ID"
        const val EXTRA_MODE = "ClipListActivity.EXTRA_MODE"
        const val EXTRA_ADD_TAB_TO_ACCOUNT_ID = "ClipListActivity.EXTRA_ADD_TAB_TO_ACCOUNT_ID"
    }

    override fun newIntent(args: ClipListNavigationArgs): Intent {
        return Intent(activity, ClipListActivity::class.java).apply {
            args.accountId?.let {
                putExtra(EXTRA_ACCOUNT_ID, it)
            }
            putExtra(EXTRA_ADD_TAB_TO_ACCOUNT_ID, args.addTabToAccountId)
            putExtra(EXTRA_MODE, args.mode.name)
        }
    }

}