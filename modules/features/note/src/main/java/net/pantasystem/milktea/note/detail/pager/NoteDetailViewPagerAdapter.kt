package net.pantasystem.milktea.note.detail.pager

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note

class NoteDetailViewPagerAdapter(activity: Fragment, val factory: PageableFragmentFactory) : FragmentStateAdapter(activity) {
    private var _list: List<Note.Id> = emptyList()

    override fun createFragment(position: Int): Fragment {
        return factory.create(accountId = _list[position].accountId, Pageable.Show(noteId = _list[position].noteId))
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    fun submitList(newList: List<Note.Id>) {
        val oldList = _list
        _list = newList
        val callback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        }
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }

    override fun containsItem(itemId: Long): Boolean {
        return _list.map {
            it.hashCode().toLong()
        }.contains(itemId)
    }

    override fun getItemId(position: Int): Long {
        return _list[position].hashCode().toLong()
    }
}