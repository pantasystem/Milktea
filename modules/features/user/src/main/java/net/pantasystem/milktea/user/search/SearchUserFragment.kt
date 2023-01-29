package net.pantasystem.milktea.user.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.user.UserCardListActionHandler
import net.pantasystem.milktea.user.compose.UserDetailCardList
import net.pantasystem.milktea.user.compose.UserDetailCardListAction
import net.pantasystem.milktea.user.viewmodel.ToggleFollowViewModel

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchUserFragment : Fragment() {

    companion object {
        private const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.ui.users.SearchUserFragment"

        @JvmStatic
        fun newInstance(userName: String): SearchUserFragment {
            return SearchUserFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_USER_NAME, userName)
                }
            }
        }
    }


    val viewModel: SearchUserViewModel by viewModels()

    private val toggleFollowViewModel: ToggleFollowViewModel by viewModels()

    val username: String? by lazy {
        arguments?.getString(EXTRA_USER_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val users by viewModel.users.collectAsState()
                    val state by viewModel.searchState.collectAsState()
                    val account by viewModel.currentAccount.collectAsState()

                    UserDetailCardList(
                        pageableState = state,
                        users = users,
                        isUserNameMain = false,
                        accountHost = account?.getHost(),
                        myId = account?.remoteId,
                        onAction = ::onAction
                    )
                }
            }
        }.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (username != null) {
            viewModel.setUserName(username!!)
        }

    }



    fun onAction(it: UserDetailCardListAction) {
        UserCardListActionHandler(requireActivity(), toggleFollowViewModel) {
            viewModel.search()
        }.onAction(it)

    }




}