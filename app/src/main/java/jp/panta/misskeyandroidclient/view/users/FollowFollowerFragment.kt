package jp.panta.misskeyandroidclient.view.users

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.databinding.FragmentFollowFollwerBinding
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class FollowFollowerFragment : Fragment(R.layout.fragment_follow_follwer){

    companion object{
        private const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment.EXTRA_USER_ID"
        private const val EXTRA_TYPE = "jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment.EXTRA_TYPE"
        fun newInstance(type: FollowFollowerViewModel.Type, userId: User.Id) : FollowFollowerFragment{
            return FollowFollowerFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_USER_ID, userId)
                    putInt(EXTRA_TYPE, type.ordinal)
                }
            }

        }
    }

    private var mViewModel: FollowFollowerViewModel? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private val mBinding: FragmentFollowFollwerBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeOrdinal = arguments?.getInt(EXTRA_TYPE)?: 0

        val type = FollowFollowerViewModel.Type.values()[typeOrdinal]
        //val user = arguments?.getSerializable(EXTRA_USER) as UserDTO?
        val userId = arguments?.getSerializable(EXTRA_USER_ID) as User.Id

        mLinearLayoutManager = LinearLayoutManager(view.context)

        mBinding.followFollowerList.layoutManager = mLinearLayoutManager
        mBinding.followFollowerList.addOnScrollListener(mScrollListener)


        val miApplication = context?.applicationContext as MiApplication

        val followFollowerViewModel = ViewModelProvider(this, FollowFollowerViewModel.Factory(userId, type, miApplication))[FollowFollowerViewModel::class.java]
        mViewModel = followFollowerViewModel


        val adapter = FollowableUserListAdapter(
            viewLifecycleOwner, followFollowerViewModel, ViewModelProvider(this, ToggleFollowViewModel.Factory(miApplication))[ToggleFollowViewModel::class.java]
        )

        mBinding.followFollowerList.adapter = adapter

        followFollowerViewModel.users.observe(viewLifecycleOwner,  {
            adapter.submitList(it)
        })

        followFollowerViewModel.loadInit()

        mViewModel?.isInitializing?.observe(viewLifecycleOwner,  {
            mBinding.swipeRefresh.isRefreshing = it
        })

        mViewModel?.showUserEventBus?.observe(viewLifecycleOwner,  {
            val intent = UserDetailActivity.newInstance(requireActivity(), userId = it)
            intent.putActivity(Activities.ACTIVITY_IN_APP)

            requireActivity().startActivity(intent)
        })

        mBinding.swipeRefresh.setOnRefreshListener {
            mViewModel?.loadInit()
        }

    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private val mScrollListener = object : RecyclerView.OnScrollListener(){
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
                mViewModel?.loadOld()

            }

        }
    }
}