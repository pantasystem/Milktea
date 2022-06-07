package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.Activities
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.databinding.FragmentFollowFollwerBinding
import jp.panta.misskeyandroidclient.putActivity
import jp.panta.misskeyandroidclient.ui.users.viewmodel.FollowFollowerViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.provideFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.RequestType
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.from
import net.pantasystem.milktea.model.user.string
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FollowFollowerFragment : Fragment(R.layout.fragment_follow_follwer){

    companion object{
        private const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.ui.users.FollowFollowerFragment.EXTRA_USER_ID"
        private const val EXTRA_TYPE = "jp.panta.misskeyandroidclient.ui.users.FollowFollowerFragment.EXTRA_TYPE"
        fun newInstance(type: RequestType) : FollowFollowerFragment{
            return FollowFollowerFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_USER_ID, type.userId)
                    putString(EXTRA_TYPE, type.string())
                }
            }

        }
    }

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private val _binding: FragmentFollowFollwerBinding by dataBinding()


    @Inject lateinit var viewModelFactory: FollowFollowerViewModel.ViewModelAssistedFactory
    val followFollowerViewModel by viewModels<FollowFollowerViewModel> {
        val strType = arguments?.getString(EXTRA_TYPE) ?: "following"
        val userId = arguments?.getSerializable(EXTRA_USER_ID) as User.Id

        val type = RequestType.from(strType, userId)
        FollowFollowerViewModel.provideFactory(viewModelFactory, type)
    }

    val viewModel: ToggleFollowViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mLinearLayoutManager = LinearLayoutManager(view.context)

        _binding.followFollowerList.layoutManager = mLinearLayoutManager
        _binding.followFollowerList.addOnScrollListener(_scrollListener)


        val adapter = FollowableUserListAdapter(
            viewLifecycleOwner, followFollowerViewModel,
        ) {
            viewModel.toggleFollow(it)
        }

        _binding.followFollowerList.adapter = adapter

        followFollowerViewModel.users.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        followFollowerViewModel.loadInit()


        lifecycleScope.launchWhenResumed {
            followFollowerViewModel.isInitialLoading.collect {
                _binding.swipeRefresh.isRefreshing = it
            }
        }



        followFollowerViewModel

        followFollowerViewModel.showUserEventBus.observe(viewLifecycleOwner) {
            val intent = UserDetailActivity.newInstance(requireActivity(), userId = it)
            intent.putActivity(Activities.ACTIVITY_IN_APP)

            requireActivity().startActivity(intent)
        }

        _binding.swipeRefresh.setOnRefreshListener {
            followFollowerViewModel.loadInit()
        }

    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private val _scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount


            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                followFollowerViewModel.loadOld()

            }

        }
    }
}