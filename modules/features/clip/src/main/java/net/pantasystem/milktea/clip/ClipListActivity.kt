package net.pantasystem.milktea.clip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.ClipListNavigation
import net.pantasystem.milktea.common_navigation.ClipListNavigationArgs
import javax.inject.Inject

@AndroidEntryPoint
class ClipListActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

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

            MdcTheme {
                Scaffold() {
                    LazyColumn(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        when(val content = uiState.clipStatusesState.content) {
                            is StateContent.Exist -> {
                                items(content.rawContent) { clipState ->
                                    ClipTile(
                                        clipState = clipState,
                                        isSelectMode = mode != ClipListNavigationArgs.Mode.View,
                                        isSelected = mode != ClipListNavigationArgs.Mode.View && clipState.isAddedToTab,
                                        onClick = {
                                            clipListViewModel.onClipTileClicked(clipState)
                                        },
                                        onAddToTabButtonClicked = {
                                            clipListViewModel.onToggleAddToTabButtonClicked(clipState)
                                        }
                                    )
                                }
                            }
                            is StateContent.NotExist -> {
                                item {
                                    Box(contentAlignment = Alignment.Center) {
                                        when(uiState.clipStatusesState) {
                                            is ResultState.Error -> {
                                                Text("Load error")
                                            }
                                            is ResultState.Fixed -> {
                                                Text("Clip is not exists")
                                            }
                                            is ResultState.Loading -> {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}

class ClipListNavigationImpl @Inject constructor(
    private val activity: Activity
): ClipListNavigation {
    companion object {
        const val EXTRA_ACCOUNT_ID = "ClipListActivity.EXTRA_ACCOUNT_ID"
        const val EXTRA_MODE = "ClipListActivity.EXTRA_MODE"
    }
    override fun newIntent(args: ClipListNavigationArgs): Intent {
        return Intent(activity, ClipListActivity::class.java).apply {
            args.accountId?.let {
                putExtra(EXTRA_ACCOUNT_ID, it)
            }
            putExtra(EXTRA_MODE, args.mode.name)
        }
    }

}