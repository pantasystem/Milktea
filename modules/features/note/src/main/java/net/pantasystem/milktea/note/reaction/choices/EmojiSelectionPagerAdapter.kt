@file:Suppress("DEPRECATION")
package net.pantasystem.milktea.note.reaction.choices

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.reaction.viewmodel.TabType

class ReactionChoicesPagerAdapter(fragmentManager: FragmentManager, val context: Context) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var categoryList: List<TabType> = emptyList()
    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(val type = categoryList[position]) {
            TabType.All -> context.getString(R.string.all)
            is TabType.Category -> type.name
            TabType.OftenUse -> context.getString(R.string.often_use)
            TabType.UserCustom -> context.getString(R.string.user)
        }
    }

    override fun getItem(position: Int): Fragment {
        return when(val type = categoryList[position]) {
            TabType.All -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.DEFAULT)
            is TabType.Category -> ReactionChoicesFragment.newInstance(
                ReactionChoicesFragment.Type.CATEGORY,
                type.name,
            )
            TabType.OftenUse -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.FREQUENCY)
            TabType.UserCustom -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.USER)
        }

    }

    fun setList(list: List<TabType>) {
        categoryList = list
        notifyDataSetChanged()
    }

}