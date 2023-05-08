package net.pantasystem.milktea.search.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.user.UserCardActionHandler
import net.pantasystem.milktea.user.compose.UserDetailCard
import net.pantasystem.milktea.user.compose.UserDetailCardAction
import net.pantasystem.milktea.user.viewmodel.ToggleFollowViewModel

@AndroidEntryPoint
class ExploreFragment : Fragment() {


    private val exploreViewModel: ExploreViewModel by viewModels()
    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()

    companion object {
        fun newInstance(type: ExploreType): ExploreFragment {
            return ExploreFragment().apply {
                arguments = Bundle().apply {
                    putInt("type", type.ordinal)
                }
            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by exploreViewModel.uiState.collectAsState()
                val account by exploreViewModel.account.collectAsState()

                MdcTheme {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(rememberNestedScrollInteropConnection())
                    ) {
                        for(item in uiState.states) {
                            stickyHeader {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        getString(item.title),
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                            when(val content = item.loadingState.content) {
                                is StateContent.Exist -> {
                                    items(content.rawContent.size) { i ->
                                        UserDetailCard(
                                            userDetail = content.rawContent[i],
                                            isUserNameMain = false,
                                            accountHost = account?.getHost(),
                                            myId = account?.remoteId,
                                            onAction = ::onAction
                                        )
                                    }
                                }
                                is StateContent.NotExist -> {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            when(val state = item.loadingState) {
                                                is ResultState.Error -> {
                                                    Text("load error")
                                                    Text(state.throwable.toString())
                                                }
                                                is ResultState.Fixed -> Text("no users")
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
        }.rootView
    }


    fun onAction(event: UserDetailCardAction) {
        UserCardActionHandler(requireActivity(), toggleFollowViewModel)
            .onAction(event)
    }
}

enum class ExploreType {
    Local, Fediverse,
}

@Composable
fun getString(src: StringSource): String {
    return src.getString(LocalContext.current)
}