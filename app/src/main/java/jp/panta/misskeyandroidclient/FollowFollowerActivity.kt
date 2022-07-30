@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityFollowFollowerBinding
import jp.panta.misskeyandroidclient.ui.TitleSettable
import jp.panta.misskeyandroidclient.ui.users.FollowFollowerFragment
import jp.panta.misskeyandroidclient.ui.users.ToggleFollowErrorHandler
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserDetailViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.provideFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.user.RequestType
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@AndroidEntryPoint
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

    lateinit var mBinding: ActivityFollowFollowerBinding

    @Inject
    lateinit var assistedFactory: UserDetailViewModel.ViewModelAssistedFactory

    private val userDetailViewModel: UserDetailViewModel by viewModels {
        val userId = intent.getSerializableExtra(EXTRA_USER_ID) as User.Id
        UserDetailViewModel.provideFactory(assistedFactory, userId)
    }

    private val toggleFollowFollowerViewModel: ToggleFollowViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_follow_follower)
        setSupportActionBar(mBinding.followFollowerToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val userId = intent.getSerializableExtra(EXTRA_USER_ID) as User.Id

        userDetailViewModel.user.observe(this) {
            title = it?.displayName
        }

        mBinding.followFollowerPager.adapter = FollowFollowerPagerAdapter(userId)
        mBinding.followFollowerTab.setupWithViewPager(mBinding.followFollowerPager)
        mBinding.followFollowerPager.currentItem = intent.getIntExtra(EXTRA_VIEW_CURRENT, FOLLOWER_VIEW_MODE)

        val errorHandler = ToggleFollowErrorHandler(mBinding.layoutBase) {
            toggleFollowFollowerViewModel.toggleFollow(it)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                toggleFollowFollowerViewModel.errors.collect(errorHandler::invoke)
            }
        }

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

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        override fun getItem(position: Int): Fragment {
            return if(position == 0){
                FollowFollowerFragment.newInstance(RequestType.Following(userId))
            }else{
                FollowFollowerFragment.newInstance(RequestType.Follower(userId))
            }
        }
    }

}
