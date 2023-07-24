package net.pantasystem.milktea.user.profile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.user.reaction.UserReactionsFragment
import net.pantasystem.milktea.user.profile.viewmodel.UserDetailTabType

class ProfileTabPagerAdapter(
    private val pageableFragmentFactory: PageableFragmentFactory,
    private val userPinnedNotesFragmentFactory: UserPinnedNotesFragmentFactory,
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    var tabs: List<UserDetailTabType> = emptyList()
        private set

    override fun createFragment(position: Int): Fragment {
        return when (val tab = tabs[position]) {
            is UserDetailTabType.Gallery -> pageableFragmentFactory.create(
                tab.accountId,
                Pageable.Gallery.User(tab.userId.id),
            )
            is UserDetailTabType.Media -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.UserTimeline(
                    tab.userId.id,
                    withFiles = true
                )
            )
            is UserDetailTabType.PinNote -> userPinnedNotesFragmentFactory.create(tab.userId)
            is UserDetailTabType.Reactions -> UserReactionsFragment.newInstance(tab.userId)
            is UserDetailTabType.UserTimeline -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = false
                )
            )
            is UserDetailTabType.UserTimelineWithReplies -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = true
                )
            )
            is UserDetailTabType.MastodonMedia -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    isOnlyMedia = true,
                )
            )
            is UserDetailTabType.MastodonUserTimeline -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = true,
                )
            )
            is UserDetailTabType.MastodonUserTimelineWithReplies -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = false,
                )
            )
            is UserDetailTabType.MastodonUserTimelineOnlyPosts -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReblogs = true,
                )
            )
            is UserDetailTabType.UserTimelineOnlyPosts -> pageableFragmentFactory.create(
                tab.userId.accountId,
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeMyRenotes = false,
                ),
            )
        }

    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun submitList(list: List<UserDetailTabType>) {

        val old = tabs
        tabs = list
        val callback = object : DiffUtil.Callback() {
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }

            override fun getNewListSize(): Int {
                return list.size
            }

            override fun getOldListSize(): Int {
                return old.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }
        }
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }


}
