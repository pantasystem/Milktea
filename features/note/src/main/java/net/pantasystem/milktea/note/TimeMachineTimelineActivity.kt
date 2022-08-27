package net.pantasystem.milktea.note

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.datetime.Instant
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_navigation.TimeMachineArgs
import net.pantasystem.milktea.common_navigation.TimeMachineNavigation
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.databinding.ActivityTimemachineTimelineBinding
import javax.inject.Inject

@AndroidEntryPoint
class TimeMachineTimelineActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var accountStore: AccountStore

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    val binding: ActivityTimemachineTimelineBinding by dataBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_timemachine_timeline)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val initialStartAt = intent.getLongExtra("EXTRA_INITIAL_START_AT", -1).let {
            if (it >= 0) {
                Instant.fromEpochMilliseconds(it)
            } else {
                null
            }
        }

        if (initialStartAt == null) {
            finish()
            return
        }

        val adapter = TimeMachineViewPagerAdapter(
            this,
            pageableFragmentFactory,
            listOf(
                Pageable.HomeTimeline(),
                Pageable.LocalTimeline(),
                Pageable.HybridTimeline(),
                Pageable.GlobalTimeline()
            ),
            initialStartAt
        )
        binding.viewPager.adapter = adapter

    }
}

class TimeMachineViewPagerAdapter(
    activity: AppCompatActivity,
    val pageableFragmentFactory: PageableFragmentFactory,
    val pageables: List<Pageable>,
    val startAt: Instant
) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return pageableFragmentFactory.create(pageables[position], startAt)
    }

    override fun getItemCount(): Int {
        return pageables.size
    }


}

class TimeMachineNavigationImpl @Inject constructor(
    val activity: Activity
) : TimeMachineNavigation {
    override fun newIntent(args: TimeMachineArgs): Intent {
        return Intent(activity, TimeMachineTimelineActivity::class.java).apply {
            putExtra("EXTRA_INITIAL_START_AT", args.initialStartAt)
        }
    }
}