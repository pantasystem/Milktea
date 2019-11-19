package jp.panta.misskeyandroidclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityUserDetailBinding
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.view.users.PinNoteFragment
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_user_detail.*
import java.lang.IllegalArgumentException

class UserDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(this, R.layout.activity_user_detail)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val userId = intent.getStringExtra(EXTRA_USER_ID)

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {ci ->
            val viewModel = ViewModelProvider(this, UserDetailViewModelFactory(ci, miApplication, userId))[UserDetailViewModel::class.java]
            binding.userViewModel = viewModel

            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ci, miApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel)
                .initViewModelListener()

            viewModel.load()
            val adapter =UserTimelinePagerAdapter(supportFragmentManager, ci, userId)
            //userTimelinePager.adapter = adapter
            binding.userTimelinePager.adapter = adapter
            binding.userTimelineTab.setupWithViewPager(binding.userTimelinePager)

            viewModel.userName.observe(this, Observer{
                supportActionBar?.title = it
            })
            //userTimelineTab.setupWithViewPager()

        })

        /*val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_base, TimelineFragment.newInstance(NoteRequest.Setting(type= NoteType.LOCAL)))
        ft.commit()*/
    }

    inner class UserTimelinePagerAdapter(
        fm: FragmentManager,
        val connectionInstance: ConnectionInstance,
        val userId: String
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val titles = listOf(getString(R.string.post), getString(R.string.pin), getString(R.string.media))
        private val requestMedia = NoteRequest.Setting(
            NoteType.USER,
            withFiles = true,
            userId = userId
        )
        private val requestTimeline = NoteRequest.Setting(
            NoteType.USER,
            userId = userId
        )
        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> TimelineFragment.newInstance(requestTimeline)
                1 -> PinNoteFragment()
                2 -> TimelineFragment.newInstance(requestMedia)
                else -> throw IllegalArgumentException("こんなものはない！！")
            }
        }
    }
}
