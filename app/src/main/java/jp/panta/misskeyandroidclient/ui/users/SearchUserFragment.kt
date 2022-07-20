package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

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

                    UserDetailCardList(
                        pageableState = state,
                        users = users,
                        isUserNameMain = false,
                        onAction = ::onAction
                    )
                }
            }
        }.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            viewModel.errors.collect {
                Toast.makeText(requireContext(), "Load error:$it", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun onAction(it: UserDetailCardListAction) {
        UserCardListActionHandler(requireActivity(), toggleFollowViewModel) {
            viewModel.search()
        }.onAction(it)

    }




}