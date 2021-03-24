package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.TitleSettable
import jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_follow_follower.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class FollowFollowerActivity : AppCompatActivity(), TitleSettable {

    companion object{
        private const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.FollowFollowerActivity.EXTRA_USER_ID"
        private const val EXTRA_VIEW_CURRENT = "jp.panta.misskeyandroidclient.FollowFollowerActivity.EXTRA_VIEW_CURRENT"
        private const val FOLLOWING_VIEW_MODE = 0
        private const val FOLLOWER_VIEW_MODE = 1

        fun newIntent(context: Context, userId: User.Id, isFollowing: Boolean): Intent {
            return Intent(context, FollowFollowerActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_VIEW_CURRENT, if(isFollowing) FOLLOWING_VIEW_MODE else FOLLOWER_VIEW_MODE)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_follow_follower)
        setSupportActionBar(follow_follower_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //val user = intent.getSerializableExtra(EXTRA_USER) as UserDTO?
        val userId = intent.getSerializableExtra(EXTRA_USER_ID) as User.Id

        val miApplication = application as MiApplication
        val userDetailViewModel = ViewModelProvider(this, UserDetailViewModelFactory(miApplication, userId, null))[UserDetailViewModel::class.java]
        userDetailViewModel.user.observe(this) {
            setTitle(it.getDisplayName())
        }

        follow_follower_pager.adapter = FollowFollowerPagerAdapter(userId)
        follow_follower_tab.setupWithViewPager(follow_follower_pager)
        follow_follower_pager.currentItem = intent.getIntExtra(EXTRA_VIEW_CURRENT, FOLLOWER_VIEW_MODE)


    }

    override fun setTitle(text: String) {
        supportActionBar?.title = text
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    inner class FollowFollowerPagerAdapter(val userId: User.Id) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val titleList = arrayOf(getString(R.string.following), getString(R.string.follower))

        override fun getCount(): Int {
            return titleList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titleList[position]
        }

        override fun getItem(position: Int): Fragment {
            return if(position == 0){
                FollowFollowerFragment.newInstance(FollowFollowerViewModel.Type.FOLLOWING, userId)
            }else{
                FollowFollowerFragment.newInstance(FollowFollowerViewModel.Type.FOLLOWER, userId)
            }
        }
    }

}
