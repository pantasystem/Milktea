package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.users.FollowFollowerFragment
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel
import kotlinx.android.synthetic.main.activity_follow_follower.*

class FollowFollowerActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER = "jp.panta.misskeyandroidclient.FollowFollowerActivity.EXTRA_USER"
        const val EXTRA_VIEW_CURRENT = "jp.panta.misskeyandroidclient.FollowFollowerActivity.EXTRA_VIEW_CURRENT"
        const val FOLLOWING_VIEW_MODE = 0
        const val FOLLOWER_VIEW_MODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_follow_follower)
        setSupportActionBar(follow_follower_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val user = intent.getSerializableExtra(EXTRA_USER) as User?

        follow_follower_pager.currentItem = intent.getIntExtra(EXTRA_VIEW_CURRENT, FOLLOWER_VIEW_MODE)
        follow_follower_pager.adapter = FollowFollowerPagerAdapter(user)
        follow_follower_tab.setupWithViewPager(follow_follower_pager)

    }

    inner class FollowFollowerPagerAdapter(val user: User?) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val titleList = arrayOf(getString(R.string.following), getString(R.string.follower))

        override fun getCount(): Int {
            return titleList.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }

        override fun getItem(position: Int): Fragment {
            return if(position == 0){
                FollowFollowerFragment.newInstance(FollowFollowerViewModel.Type.FOLLOWING, user)
            }else{
                FollowFollowerFragment.newInstance(FollowFollowerViewModel.Type.FOLLOWER, user)
            }
        }
    }

}
