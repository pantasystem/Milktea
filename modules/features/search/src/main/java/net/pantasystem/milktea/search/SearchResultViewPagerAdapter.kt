package net.pantasystem.milktea.search

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.user.search.SearchUserFragment


class SearchResultViewPagerAdapter(
    activity: FragmentActivity,
    private val pageableFragmentFactory: PageableFragmentFactory,
) : FragmentStateAdapter(activity) {

    var items: List<SearchResultTabItem> = emptyList()
        private set


    override fun createFragment(position: Int): Fragment {
        val item = items[position]
        return when (item.type) {
            SearchResultTabItem.Type.SearchMisskeyPosts -> pageableFragmentFactory.create(
                Pageable.Search(query = item.query, userId = item.userId)
            )
            SearchResultTabItem.Type.SearchMisskeyPostsByTag -> pageableFragmentFactory.create(
                Pageable.SearchByTag(tag = item.query)
            )
            SearchResultTabItem.Type.SearchMisskeyPostsWithFilesByTag -> pageableFragmentFactory.create(
                Pageable.SearchByTag(tag = item.query, withFiles = true)
            )
            SearchResultTabItem.Type.SearchMisskeyUsers -> SearchUserFragment.newInstance(item.query)
            SearchResultTabItem.Type.SearchMastodonPosts -> pageableFragmentFactory.create(
                Pageable.Mastodon.SearchTimeline(
                    item.query,
                    userId = item.userId
                )
            )
            SearchResultTabItem.Type.SearchMastodonPostsByTag -> pageableFragmentFactory.create(
                Pageable.Mastodon.TagTimeline(item.query)
            )
            SearchResultTabItem.Type.SearchMastodonUsers -> SearchUserFragment.newInstance(item.query)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(list: List<SearchResultTabItem>) {
        val old = items
        items = list
        val callback = object : DiffUtil.Callback() {
            override fun getNewListSize(): Int {
                return list.size
            }

            override fun getOldListSize(): Int {
                return old.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }
        }
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return items.map {
            it.hashCode().toLong()
        }.contains(itemId)
    }
}