package net.pantasystem.milktea.search.trend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_navigation.SearchNavType
import net.pantasystem.milktea.common_navigation.SearchNavigation
import javax.inject.Inject

@AndroidEntryPoint
class TrendFragment : Fragment() {

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
                MdcTheme {
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
                                    Surface(
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                requireActivity().startActivity(
                                                    searchNavigation.newIntent(
                                                        SearchNavType.ResultScreen(
                                                            "#${item.name}"
                                                        )
                                                    )
                                                )
                                            }
                                    ) {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    vertical = 12.dp,
                                                    horizontal = 14.dp
                                                )
                                        ) {
                                            Text(
                                                "#${item.name}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("${item.usersCount}人が投稿")
                                        }
                                    }
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