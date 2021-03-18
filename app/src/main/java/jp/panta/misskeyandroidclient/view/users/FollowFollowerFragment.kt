package jp.panta.misskeyandroidclient.view.users

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import kotlinx.android.synthetic.main.fragment_follow_follwer.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

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

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeOrdinal = arguments?.getInt(EXTRA_TYPE)?: 0

        val type = FollowFollowerViewModel.Type.values()[typeOrdinal]
        //val user = arguments?.getSerializable(EXTRA_USER) as UserDTO?
        val userId = arguments?.getSerializable(EXTRA_USER_ID) as User.Id

        mLinearLayoutManager = LinearLayoutManager(view.context)

        follow_follower_list.layoutManager = mLinearLayoutManager
        follow_follower_list.addOnScrollListener(mScrollListener)


        val miApplication = context?.applicationContext as MiApplication

        val followFollowerViewModel = ViewModelProvider(this, FollowFollowerViewModel.Factory(userId, type, miApplication))[FollowFollowerViewModel::class.java]
        mViewModel = followFollowerViewModel


        val adapter = FollowableUserListAdapter(
            viewLifecycleOwner, followFollowerViewModel, ViewModelProvider(this, ToggleFollowViewModel.Factory(miApplication))[ToggleFollowViewModel::class.java]
        )

        follow_follower_list.adapter = adapter

        followFollowerViewModel.users.observe(viewLifecycleOwner,  {
            adapter.submitList(it)
        })

        followFollowerViewModel.loadInit()

        mViewModel?.isInitializing?.observe(viewLifecycleOwner,  {
            swipe_refresh.isRefreshing = it
        })

        mViewModel?.showUserEventBus?.observe(viewLifecycleOwner,  {
            val intent = UserDetailActivity.newInstance(requireActivity(), userId = it)
            intent.putActivity(Activities.ACTIVITY_IN_APP)

            requireActivity().startActivity(intent)
        })

        swipe_refresh.setOnRefreshListener {
            mViewModel?.loadInit()
        }

    }

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