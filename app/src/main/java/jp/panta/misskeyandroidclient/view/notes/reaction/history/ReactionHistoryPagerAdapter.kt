package jp.panta.misskeyandroidclient.view.notes.reaction.history

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import jp.panta.misskeyandroidclient.model.notes.Note

class ReactionHistoryPagerAdapter(
    fragmentManager: FragmentManager,
    val types: List<ViewType>,
    val noteId: Note.Id
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){



    override fun getCount(): Int {
        return types.size
    }

    override fun getItem(position: Int): Fragment {
        return when(val type = types[position]) {
            is ViewType.All -> ReactionHistoryListFragment.newInstance(noteId)
            is ViewType.Type -> ReactionHistoryListFragment.newInstance(noteId, type.type)
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when(val type = types[position]){
            is ViewType.All -> "All"
            is ViewType.Type -> type.type
        }
    }
}

sealed class ViewType {
    object All : ViewType()
    data class Type(val type: String) : ViewType()
}