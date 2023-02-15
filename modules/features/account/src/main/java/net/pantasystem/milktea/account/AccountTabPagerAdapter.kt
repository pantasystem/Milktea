package net.pantasystem.milktea.account

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import net.pantasystem.milktea.messaging.MessagingHistoryFragment
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.user.reaction.UserReactionsFragment


class AccountTabPagerAdapter(
    val pageableFragmentFactory: PageableFragmentFactory,
    val userPinnedNotesFragmentFactory: UserPinnedNotesFragmentFactory,
    activity: Fragment,
) : FragmentStateAdapter(activity) {

    var tabs: List<AccountTabTypes> = emptyList()
        private set

    override fun createFragment(position: Int): Fragment {
        return when (val tab = tabs[position]) {
            is AccountTabTypes.Gallery -> pageableFragmentFactory.create(
                tab.accountId,
                Pageable.Gallery.User(tab.userId.id),
            )
            is AccountTabTypes.Media -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    withFiles = true
                )
            )
            is AccountTabTypes.PinNote -> userPinnedNotesFragmentFactory.create(tab.userId)
            is AccountTabTypes.Reactions -> UserReactionsFragment.newInstance(tab.userId)
            is AccountTabTypes.UserTimeline -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = false
                )
            )
            is AccountTabTypes.UserTimelineWithReplies -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = true
                )
            )
            is AccountTabTypes.MastodonMedia -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    isOnlyMedia = true,
                )
            )
            is AccountTabTypes.MastodonUserTimeline -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = true,
                )
            )
            is AccountTabTypes.MastodonUserTimelineWithReplies -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = false,
                )
            )
            AccountTabTypes.Account -> AccountFragment()
            AccountTabTypes.Message -> MessagingHistoryFragment()
        }

    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun submitList(list: List<AccountTabTypes>) {

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
