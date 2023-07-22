package net.pantasystem.milktea.search.trend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.SearchNavType
import net.pantasystem.milktea.common_navigation.SearchNavigation
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@AndroidEntryPoint
class TrendFragment : Fragment() {

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    @Inject
    lateinit var searchNavigation: SearchNavigation

    private val viewModel by viewModels<TrendViewModel>()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val uiState by viewModel.uiState.collectAsState()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(rememberNestedScrollInteropConnection())
                    ) {
                        when (val content = uiState.trendTags.content) {
                            is StateContent.Exist -> {
                                items(content.rawContent.size) { index ->
                                    val item = content.rawContent[index]
                                    HashtagTrendItem(hashtag = item, onClick = {
                                        requireActivity().startActivity(
                                            searchNavigation.newIntent(
                                                SearchNavType.ResultScreen(
                                                    "#${item.name}"
                                                )
                                            )
                                        )
                                    })
                                }
                            }
                            is StateContent.NotExist -> {
                                item {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (val state = uiState.trendTags) {
                                            is ResultState.Error -> Text("Error:${state.throwable}")
                                            is ResultState.Fixed -> Text("トレンドはありません")
                                            is ResultState.Loading -> CircularProgressIndicator()
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