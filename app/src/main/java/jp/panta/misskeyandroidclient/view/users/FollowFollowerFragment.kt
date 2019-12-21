package jp.panta.misskeyandroidclient.view.users

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.UserDetailActivity
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel
import kotlinx.android.synthetic.main.fragment_follow_follwer.*

class FollowFollowerFragment : Fragment(R.layout.fragment_follow_follwer){

    companion object{
        private const val EXTRA_USER = "jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment.EXTRA_USER"
        private const val EXTRA_TYPE = "jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment.EXTRA_TYPE"
        fun newInstance(type: FollowFollowerViewModel.Type, user: User? = null) : FollowFollowerFragment{
            return FollowFollowerFragment().apply{
                arguments = Bundle().apply{
                    if(user != null){
                        putSerializable(EXTRA_USER, user)
                    }
                    putInt(EXTRA_TYPE, type.ordinal)
                }
            }

        }
    }

    private var mViewModel: FollowFollowerViewModel? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typeOrdinal = arguments?.getInt(EXTRA_TYPE)?: 0

        val type = FollowFollowerViewModel.Type.values()[typeOrdinal]
        val user = arguments?.getSerializable(EXTRA_USER) as User?

        mLinearLayoutManager = LinearLayoutManager(view.context)

        follow_follower_list.layoutManager = mLinearLayoutManager
        follow_follower_list.addOnScrollListener(mScrollListener)


        val miApplication = context?.applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(viewLifecycleOwner, Observer {ci ->
            val followFollowerViewModel = ViewModelProvider(this, FollowFollowerViewModel.Factory(ci, miApplication, user, type))[FollowFollowerViewModel::class.java]
            mViewModel = followFollowerViewModel
            val adapter = FollowFollowerListAdapter(viewLifecycleOwner, followFollowerViewModel)

            follow_follower_list.adapter = adapter
            followFollowerViewModel.followFollowerViewDataList.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })

            followFollowerViewModel.loadInit()

            mViewModel?.isInitializing?.observe(viewLifecycleOwner, Observer {
                swipe_refresh.isRefreshing = it
            })

            mViewModel?.showUserEventBus?.observe(viewLifecycleOwner, Observer {
                val intent = Intent(activity, UserDetailActivity::class.java)
                intent.putExtra(UserDetailActivity.EXTRA_USER_ID, it.id)
                activity?.startActivity(intent)
            })
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